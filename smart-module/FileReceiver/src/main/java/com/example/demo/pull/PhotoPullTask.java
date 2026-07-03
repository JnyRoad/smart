package com.example.demo.pull;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * 定时拉取照片任务。
 * <p>
 * 流程：取 token → 拉待处理清单 → 与本地目录 diff → 逐张下载（临时文件 + 原子改名）。
 * 单张下载失败按张隔离，不中断本轮其余照片的处理；401 时刷新 token 重试本轮一次。
 */
@Slf4j
@Component
public class PhotoPullTask {

    /**
     * 下载完成前的临时文件后缀，避免下游读到半成品文件。
     */
    private static final String TMP_SUFFIX = ".tmp";

    private final PhotoServerClient serverClient;
    private final OpenApiTokenClient tokenClient;
    private final PhotoPullProperties properties;

    @Autowired
    public PhotoPullTask(PhotoServerClient serverClient, OpenApiTokenClient tokenClient, PhotoPullProperties properties) {
        this.serverClient = serverClient;
        this.tokenClient = tokenClient;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${file-receiver.pull.interval-seconds:30}000")
    public void run() {
        if (!properties.getPull().isEnabled()) {
            return;
        }
        runOnce();
    }

    /**
     * 执行一轮拉取，供定时触发与单测调用。
     */
    public void runOnce() {
        String token = tokenClient.getToken();
        List<String> pendingPhotoIds;
        try {
            pendingPhotoIds = serverClient.fetchPendingPhotoIds(token);
        } catch (PhotoServerClient.UnauthorizedException e) {
            // 401/403：强制刷新 token 后重试一次
            token = tokenClient.refresh();
            pendingPhotoIds = serverClient.fetchPendingPhotoIds(token);
        }

        Path photoDir = Paths.get(properties.getPhotoDir());
        for (String photoId : pendingPhotoIds) {
            if (existsLocally(photoDir, photoId)) {
                continue;
            }
            downloadOne(photoDir, token, photoId);
        }
    }

    private boolean existsLocally(Path photoDir, String photoId) {
        return Files.exists(photoDir.resolve(photoId + ".png"));
    }

    /**
     * 下载单张照片，异常按张隔离：记 ERROR 日志后继续处理下一张，不影响本轮其余照片。
     */
    private void downloadOne(Path photoDir, String token, String photoId) {
        try {
            PhotoServerClient.DownloadResult result;
            try {
                result = serverClient.downloadPhoto(token, photoId);
            } catch (PhotoServerClient.UnauthorizedException e) {
                // 401/403：刷新 token 后对这一张重试一次
                String refreshed = tokenClient.refresh();
                result = serverClient.downloadPhoto(refreshed, photoId);
            }
            if (result.getStatus() == PhotoServerClient.DownloadResult.Status.NOT_FOUND) {
                // 404：服务端缺图，跳过该张不重试
                log.warn("照片不存在，跳过：photoId={}", photoId);
                return;
            }
            writeAtomically(photoDir, photoId, result.getBytes());
        } catch (Exception e) {
            log.error("下载照片失败，photoId={}", photoId, e);
        }
    }

    private void writeAtomically(Path photoDir, String photoId, byte[] bytes) throws IOException {
        Files.createDirectories(photoDir);
        Path target = photoDir.resolve(photoId + ".png");
        Path tmp = photoDir.resolve(photoId + ".png" + TMP_SUFFIX);
        FileUtil.writeBytes(bytes, tmp.toFile());
        Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
    }
}
