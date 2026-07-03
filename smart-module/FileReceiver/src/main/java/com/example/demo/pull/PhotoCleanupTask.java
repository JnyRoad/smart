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
 */
@Slf4j
@Component
public class PhotoCleanupTask {

    private static final String PHOTO_SUFFIX = ".png";

    private final PhotoServerClient serverClient;
    private final OpenApiTokenClient tokenClient;
    private final PhotoPullProperties properties;

    @Autowired
    public PhotoCleanupTask(PhotoServerClient serverClient, OpenApiTokenClient tokenClient, PhotoPullProperties properties) {
        this.serverClient = serverClient;
        this.tokenClient = tokenClient;
        this.properties = properties;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void run() {
        runOnce();
    }

    /**
     * 执行一轮清理，供定时触发与单测调用。
     */
    public void runOnce() {
        int retentionDays = properties.getCleanup().getRetentionDays();
        if (retentionDays <= 0) {
            // retention-days=0 表示关闭清理任务
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
            return;
        }

        Instant staleBefore = Instant.now().minusSeconds(retentionDays * 24L * 3600L);
        List<Path> candidates = listPngFiles(photoDir);
        for (Path file : candidates) {
            deleteIfStaleAndNotPending(file, staleBefore, pendingPhotoIds);
        }

        // 清理孤儿 tmp 文件
        cleanupStaleTmpFiles(photoDir);
    }

    private void deleteIfStaleAndNotPending(Path file, Instant staleBefore, Set<String> pendingPhotoIds) {
        try {
            String photoId = toPhotoId(file);
            if (pendingPhotoIds.contains(photoId)) {
                return;
            }
            FileTime mtime = Files.getLastModifiedTime(file);
            if (mtime.toInstant().isBefore(staleBefore)) {
                Files.delete(file);
                log.info("已清理过期照片：{}", file);
            }
        } catch (IOException e) {
            log.error("清理照片失败：{}", file, e);
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
     */
    private void cleanupStaleTmpFiles(Path photoDir) {
        Instant staleBefore = Instant.now().minusSeconds(1 * 24L * 3600L);
        List<Path> tmpFiles = new java.util.ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(photoDir, "*" + PHOTO_SUFFIX + ".tmp")) {
            for (Path path : stream) {
                tmpFiles.add(path);
            }
        } catch (IOException e) {
            log.error("遍历 tmp 文件目录失败：{}", photoDir, e);
            return;
        }

        for (Path tmpFile : tmpFiles) {
            try {
                FileTime mtime = Files.getLastModifiedTime(tmpFile);
                if (mtime.toInstant().isBefore(staleBefore)) {
                    Files.delete(tmpFile);
                    log.info("已清理过期孤儿 tmp 文件：{}", tmpFile);
                }
            } catch (IOException e) {
                log.error("清理 tmp 文件失败：{}", tmpFile, e);
            }
        }
    }
}
