package com.example.demo.pull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * PhotoCleanupTask 纯单测：PhotoServerClient 与 OpenApiTokenClient 全部 mock。
 */
public class PhotoCleanupTaskTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private Path photoDir;
    private PhotoServerClient serverClient;
    private OpenApiTokenClient tokenClient;
    private PhotoPullProperties properties;
    private PhotoCleanupTask task;

    @Before
    public void setUp() {
        photoDir = tempFolder.getRoot().toPath();

        serverClient = mock(PhotoServerClient.class);
        tokenClient = mock(OpenApiTokenClient.class);
        when(tokenClient.getToken()).thenReturn("token-1");

        properties = new PhotoPullProperties();
        properties.setPhotoDir(photoDir.toString());
        properties.getCleanup().setRetentionDays(7);

        task = new PhotoCleanupTask(serverClient, tokenClient, properties);
    }

    private void writeAged(String photoId, int ageDays) throws IOException {
        Path file = photoDir.resolve(photoId + ".png");
        Files.write(file, "x".getBytes());
        FileTime mtime = FileTime.from(Instant.now().minusSeconds(ageDays * 24L * 3600L + 60));
        Files.setLastModifiedTime(file, mtime);
    }

    @Test
    public void cleanupTask_deletesOnlyStaleAndNotPending() throws IOException {
        // 过期且不在 pending 清单 -> 删除
        writeAged("stale-not-pending", 10);
        // 过期但仍在 pending 清单 -> 保留
        writeAged("stale-but-pending", 10);
        // 未过期且不在 pending 清单 -> 保留
        writeAged("fresh-not-pending", 1);

        when(serverClient.fetchPendingPhotoIds("token-1"))
                .thenReturn(Collections.singletonList("stale-but-pending"));

        task.runOnce();

        assertThat(photoDir.resolve("stale-not-pending.png")).doesNotExist();
        assertThat(photoDir.resolve("stale-but-pending.png")).exists();
        assertThat(photoDir.resolve("fresh-not-pending.png")).exists();
    }

    @Test
    public void cleanupTask_skippedWhenPendingFetchFails() throws IOException {
        writeAged("stale-not-pending", 10);
        when(serverClient.fetchPendingPhotoIds("token-1"))
                .thenThrow(new RuntimeException("清单接口异常"));

        task.runOnce();

        // 清单拉取失败：跳过本轮清理，文件应保留
        assertThat(photoDir.resolve("stale-not-pending.png")).exists();
    }

    @Test
    public void cleanupTask_disabledWhenRetentionZero() throws IOException {
        writeAged("stale-not-pending", 10);
        properties.getCleanup().setRetentionDays(0);

        task.runOnce();

        assertThat(photoDir.resolve("stale-not-pending.png")).exists();
        // retention=0 关闭清理时不应该去调用清单接口
        verifyZeroInteractions(serverClient);
    }

    /**
     * 调度回归：清理任务不得使用凌晨定点 cron——部署机是仅工作时间开机的 Windows 台式机，
     * 凌晨永远不在线，cron 永远不会触发，清理会静默失效。
     * 必须改为「启动延迟首跑 + 固定间隔」：只要白天开机就必然轮到。
     */
    @Test
    public void cleanupTask_scheduledByFixedIntervalNotOffHoursCron() throws NoSuchMethodException {
        Scheduled scheduled = PhotoCleanupTask.class.getMethod("run").getAnnotation(Scheduled.class);
        assertThat(scheduled.cron()).isEmpty();
        assertThat(scheduled.fixedDelayString()).isEqualTo("${file-receiver.cleanup.interval-seconds:14400}000");
        // 启动后延迟 1 分钟首跑：错开启动初期的拉取首轮，又不至于让急需的清理等太久
        assertThat(scheduled.initialDelay()).isEqualTo(60L * 1000L);
    }

    /** 配置字段默认值与 @Scheduled 占位符兜底值互相交叉比对，防止两处默认各自漂移（不能各钉各的字面量） */
    @Test
    public void cleanupProperties_defaultIntervalMatchesScheduleFallback() throws NoSuchMethodException {
        Scheduled scheduled = PhotoCleanupTask.class.getMethod("run").getAnnotation(Scheduled.class);
        // 从 "${file-receiver.cleanup.interval-seconds:14400}000" 中截取占位符兜底秒数
        String delayString = scheduled.fixedDelayString();
        String fallbackSeconds = delayString.substring(delayString.indexOf(':') + 1, delayString.indexOf('}'));
        assertThat(fallbackSeconds)
                .isEqualTo(String.valueOf(new PhotoPullProperties().getCleanup().getIntervalSeconds()));
    }

    @Test
    public void cleanupTask_removesStaleOrphanTmpFiles() throws IOException {
        // 超期 tmp 文件（>1天）应被删除
        Path staleTmpFile = photoDir.resolve("stale-tmp.png.tmp");
        Files.write(staleTmpFile, "x".getBytes());
        FileTime staleMtime = FileTime.from(Instant.now().minusSeconds(2 * 24L * 3600L));
        Files.setLastModifiedTime(staleTmpFile, staleMtime);

        // 新鲜 tmp 文件（<1天）应保留
        Path freshTmpFile = photoDir.resolve("fresh-tmp.png.tmp");
        Files.write(freshTmpFile, "x".getBytes());
        FileTime freshMtime = FileTime.from(Instant.now().minusSeconds(12 * 3600L));
        Files.setLastModifiedTime(freshTmpFile, freshMtime);

        when(serverClient.fetchPendingPhotoIds("token-1"))
                .thenReturn(Collections.emptyList());

        task.runOnce();

        assertThat(staleTmpFile).doesNotExist();
        assertThat(freshTmpFile).exists();
    }
}
