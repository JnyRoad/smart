package com.example.demo.pull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 本地照片过期清理任务。
 * <p>
 * 双条件删除：文件 mtime 超过 retention-days 且不在最新 pending 清单中，
 * 避免误删刚好还没来得及被业务方消费的照片。
 * pending 清单拉取失败时跳过本轮清理并 WARN，retention-days=0 表示关闭清理。
 * <p>
 * 调度采用「启动延迟首跑 + 固定间隔」而非凌晨定点 cron：部署机是仅工作时间开机的
 * Windows 台式机，凌晨永远不在线，定点 cron 会让清理静默失效；固定间隔保证开机当天必然执行。
 */
@Slf4j
@Component
public class PhotoCleanupTask {

    private static final String PHOTO_SUFFIX = ".png";

    /**
     * 启动后首轮清理的延迟：1 分钟。错开启动初期的拉取首轮，又不至于让急需的清理（如磁盘吃紧时重启触发）等太久。
     * 包内可见：StartupConfigValidator 的启动摘要日志从这里换算首跑时间，避免文案与真实值漂移。
     */
    static final long STARTUP_DELAY_MILLIS = 60L * 1000L;

    /**
     * 孤儿 tmp 文件的过期余量：1 天，防止删到正在写的文件。
     */
    private static final long TMP_STALE_AGE_SECONDS = 24L * 3600L;

    private final PhotoServerClient serverClient;
    private final OpenApiTokenClient tokenClient;
    private final PhotoPullProperties properties;

    @Autowired
    public PhotoCleanupTask(PhotoServerClient serverClient, OpenApiTokenClient tokenClient, PhotoPullProperties properties) {
        this.serverClient = serverClient;
        this.tokenClient = tokenClient;
        this.properties = properties;
    }

    // 间隔默认 14400s（4 小时）。注意默认值共三处需一致：本兜底、Cleanup#intervalSeconds 字段默认
    // （这两处有单测钉住）、resources/application.properties 的环境变量兜底（运行时实际生效的一处）
    @Scheduled(initialDelay = STARTUP_DELAY_MILLIS, fixedDelayString = "${file-receiver.cleanup.interval-seconds:14400}000")
    public void run() {
        try {
            runOnce();
        } catch (Exception e) {
            // 兜底：单轮清理失败只记 ERROR，等下一轮固定间隔自动重试，不让异常打断调度
            log.error("本轮照片清理失败", e);
        }
    }

    /**
     * 执行一轮清理，供定时触发与单测调用。
     */
    public void runOnce() {
        int retentionDays = properties.getCleanup().getRetentionDays();
        if (retentionDays <= 0) {
            // retention-days=0 表示关闭清理任务
            log.info("照片过期清理未启用（retention-days={}），跳过本轮", retentionDays);
            return;
        }

        Set<String> pendingPhotoIds;
        try {
            String token = tokenClient.getToken();
            pendingPhotoIds = new HashSet<>(serverClient.fetchPendingPhotoIds(token));
        } catch (Exception e) {
            // 清单拉取失败：跳过本轮清理，避免在没有最新清单兜底的情况下误删
            log.warn("拉取待处理清单失败，跳过本轮照片清理", e);
            return;
        }

        Path photoDir = Paths.get(properties.getPhotoDir());
        if (!Files.isDirectory(photoDir)) {
            log.warn("照片目录不存在，跳过本轮清理：{}", photoDir);
            return;
        }

        log.info("开始照片过期清理：保留 {} 天，pending 保护 {} 张", retentionDays, pendingPhotoIds.size());
        Instant staleBefore = Instant.now().minusSeconds(retentionDays * 24L * 3600L);
        List<Path> candidates = listPngFiles(photoDir);
        int deletedCount = 0;
        for (Path file : candidates) {
            if (deleteIfStaleAndNotPending(file, staleBefore, pendingPhotoIds)) {
                deletedCount++;
            }
        }

        // 清理孤儿 tmp 文件
        int tmpDeletedCount = cleanupStaleTmpFiles(photoDir);
        log.info("清理轮完成：扫描 {} 张照片，删除过期 {} 张，另清理孤儿 tmp {} 个",
                candidates.size(), deletedCount, tmpDeletedCount);
    }

    /**
     * @return 该文件本轮是否被删除
     */
    private boolean deleteIfStaleAndNotPending(Path file, Instant staleBefore, Set<String> pendingPhotoIds) {
        try {
            String photoId = toPhotoId(file);
            if (pendingPhotoIds.contains(photoId)) {
                return false;
            }
            FileTime mtime = Files.getLastModifiedTime(file);
            if (mtime.toInstant().isBefore(staleBefore)) {
                Files.delete(file);
                log.info("已清理过期照片：{}", file);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("清理照片失败：{}", file, e);
            return false;
        }
    }

    private String toPhotoId(Path file) {
        String fileName = file.getFileName().toString();
        return fileName.substring(0, fileName.length() - PHOTO_SUFFIX.length());
    }

    private List<Path> listPngFiles(Path photoDir) {
        List<Path> result = new java.util.ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(photoDir, "*" + PHOTO_SUFFIX)) {
            for (Path path : stream) {
                result.add(path);
            }
        } catch (IOException e) {
            log.error("遍历照片目录失败：{}", photoDir, e);
        }
        return result;
    }

    /**
     * 清理孤儿 tmp 文件。
     * <p>
     * PhotoPullTask.writeAtomically 写盘中途失败会残留 {photoId}.png.tmp，
     * 本方法删除 photo-dir 下 mtime 超过 1 天的 *.png.tmp 文件。
     * 不查 pending 清单，因为 tmp 本来就是中间产物；1 天余量防止删到正在写的文件。
     *
     * @return 本轮删除的 tmp 文件数
     */
    private int cleanupStaleTmpFiles(Path photoDir) {
        Instant staleBefore = Instant.now().minusSeconds(TMP_STALE_AGE_SECONDS);
        List<Path> tmpFiles = new java.util.ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(photoDir, "*" + PHOTO_SUFFIX + ".tmp")) {
            for (Path path : stream) {
                tmpFiles.add(path);
            }
        } catch (IOException e) {
            log.error("遍历 tmp 文件目录失败：{}", photoDir, e);
            return 0;
        }

        int deletedCount = 0;
        for (Path tmpFile : tmpFiles) {
            try {
                FileTime mtime = Files.getLastModifiedTime(tmpFile);
                if (mtime.toInstant().isBefore(staleBefore)) {
                    Files.delete(tmpFile);
                    log.info("已清理过期孤儿 tmp 文件：{}", tmpFile);
                    deletedCount++;
                }
            } catch (IOException e) {
                log.error("清理 tmp 文件失败：{}", tmpFile, e);
            }
        }
        return deletedCount;
    }
}
