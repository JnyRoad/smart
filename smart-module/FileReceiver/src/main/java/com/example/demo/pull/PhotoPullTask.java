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
 * <p>
 * 日志约定：每轮固定输出一行 INFO 汇总（运行心跳），每张下载成功输出一行 INFO，
 * 失败输出 ERROR 带堆栈；整轮失败（token/清单环节）由 {@link #run()} 兜底记 ERROR 后等下一轮重试。
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
        try {
            runOnce();
        } catch (Exception e) {
            // 整轮兜底：token/清单环节失败只记 ERROR，等下一轮自动重试，不让异常打断调度
            log.error("本轮照片拉取失败（token 或清单环节异常，下一轮自动重试）", e);
        }
    }

    /**
     * 执行一轮拉取，供定时触发与单测调用。
     *
     * @return 本轮各结局的计数汇总（用于日志与单测断言）
     */
    public RoundSummary runOnce() {
        long roundStartMillis = System.currentTimeMillis();
        String token = tokenClient.getToken();
        List<String> pendingPhotoIds;
        try {
            pendingPhotoIds = serverClient.fetchPendingPhotoIds(token);
        } catch (PhotoServerClient.UnauthorizedException e) {
            // 401/403：强制刷新 token 后重试一次
            log.warn("拉取待处理清单时 token 失效（{}），刷新后重试一次", e.getMessage());
            token = tokenClient.refresh();
            pendingPhotoIds = serverClient.fetchPendingPhotoIds(token);
        }

        Path photoDir = Paths.get(properties.getPhotoDir());
        int alreadyExists = 0;
        int downloaded = 0;
        int missingOnServer = 0;
        int failed = 0;
        for (String photoId : pendingPhotoIds) {
            if (existsLocally(photoDir, photoId)) {
                alreadyExists++;
                continue;
            }
            DownloadOutcome outcome = downloadOne(photoDir, token, photoId);
            if (outcome == DownloadOutcome.DOWNLOADED) {
                downloaded++;
            } else if (outcome == DownloadOutcome.MISSING_ON_SERVER) {
                missingOnServer++;
            } else {
                failed++;
            }
        }

        RoundSummary summary = new RoundSummary(pendingPhotoIds.size(), alreadyExists, downloaded, missingOnServer, failed);
        // 每轮固定一行汇总 INFO：既是运行心跳，也是排查“为什么没下载”的第一现场
        log.info("拉取轮完成：待处理 {} 张，本地已有 {} 张，下载成功 {} 张，服务端缺图 {} 张，失败 {} 张，耗时 {} ms",
                summary.getPendingCount(), summary.getAlreadyExistsCount(), summary.getDownloadedCount(),
                summary.getMissingOnServerCount(), summary.getFailedCount(),
                System.currentTimeMillis() - roundStartMillis);
        return summary;
    }

    private boolean existsLocally(Path photoDir, String photoId) {
        return Files.exists(photoDir.resolve(photoId + ".png"));
    }

    /**
     * 下载单张照片，异常按张隔离：记 ERROR 日志后继续处理下一张，不影响本轮其余照片。
     */
    private DownloadOutcome downloadOne(Path photoDir, String token, String photoId) {
        long startMillis = System.currentTimeMillis();
        try {
            PhotoServerClient.DownloadResult result;
            try {
                result = serverClient.downloadPhoto(token, photoId);
            } catch (PhotoServerClient.UnauthorizedException e) {
                // 401/403：刷新 token 后对这一张重试一次
                log.warn("下载照片时 token 失效（{}），刷新后重试一次：photoId={}", e.getMessage(), photoId);
                String refreshed = tokenClient.refresh();
                result = serverClient.downloadPhoto(refreshed, photoId);
            }
            if (result.getStatus() == PhotoServerClient.DownloadResult.Status.NOT_FOUND) {
                // 404：服务端缺图，跳过该张不重试
                log.warn("照片不存在，跳过：photoId={}", photoId);
                return DownloadOutcome.MISSING_ON_SERVER;
            }
            writeAtomically(photoDir, photoId, result.getBytes());
            log.info("照片下载成功：photoId={}，大小 {} 字节，耗时 {} ms",
                    photoId, result.getBytes().length, System.currentTimeMillis() - startMillis);
            return DownloadOutcome.DOWNLOADED;
        } catch (Exception e) {
            log.error("下载照片失败，photoId={}", photoId, e);
            return DownloadOutcome.FAILED;
        }
    }

    private void writeAtomically(Path photoDir, String photoId, byte[] bytes) throws IOException {
        Files.createDirectories(photoDir);
        Path target = photoDir.resolve(photoId + ".png");
        Path tmp = photoDir.resolve(photoId + ".png" + TMP_SUFFIX);
        FileUtil.writeBytes(bytes, tmp.toFile());
        Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * 单张下载的三种结局，用于轮次汇总计数。
     */
    private enum DownloadOutcome {
        /** 下载成功并已落盘 */
        DOWNLOADED,
        /** 服务端 404 缺图，跳过不重试 */
        MISSING_ON_SERVER,
        /** 下载或落盘异常 */
        FAILED
    }

    /**
     * 一轮拉取的计数汇总：待处理总数 = 本地已有 + 下载成功 + 服务端缺图 + 失败。
     */
    public static class RoundSummary {
        private final int pendingCount;
        private final int alreadyExistsCount;
        private final int downloadedCount;
        private final int missingOnServerCount;
        private final int failedCount;

        public RoundSummary(int pendingCount, int alreadyExistsCount, int downloadedCount,
                            int missingOnServerCount, int failedCount) {
            this.pendingCount = pendingCount;
            this.alreadyExistsCount = alreadyExistsCount;
            this.downloadedCount = downloadedCount;
            this.missingOnServerCount = missingOnServerCount;
            this.failedCount = failedCount;
        }

        public int getPendingCount() {
            return pendingCount;
        }

        public int getAlreadyExistsCount() {
            return alreadyExistsCount;
        }

        public int getDownloadedCount() {
            return downloadedCount;
        }

        public int getMissingOnServerCount() {
            return missingOnServerCount;
        }

        public int getFailedCount() {
            return failedCount;
        }
    }
}
