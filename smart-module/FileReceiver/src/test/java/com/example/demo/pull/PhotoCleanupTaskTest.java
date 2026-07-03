package com.example.demo.pull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
