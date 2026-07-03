package com.example.demo.pull;

import java.util.List;

/**
 * 照片服务端 HTTP 调用的抽象接口。
 * <p>
 * 把真实网络请求隔离在实现类（{@link HutoolPhotoServerClient}）之后，
 * 测试中可注入 mock 实现，避免单测发起真实网络请求。
 */
public interface PhotoServerClient {

    /**
     * 用 client_credentials 模式换取 access token。
     *
     * @param appId     Basic 认证用户名
     * @param appSecret Basic 认证密码（禁止打日志）
     * @return token 与过期时间
     */
    TokenResult fetchToken(String appId, String appSecret);

    /**
     * 拉取待处理照片清单。
     *
     * @param accessToken Bearer token
     * @return photoId 列表
     * @throws UnauthorizedException token 失效（401/403）
     */
    List<String> fetchPendingPhotoIds(String accessToken);

    /**
     * 下载单张照片。
     *
     * @param accessToken Bearer token
     * @param photoId     照片 ID
     * @return 下载结果（含状态与字节内容）
     * @throws UnauthorizedException token 失效（401/403）
     */
    DownloadResult downloadPhoto(String accessToken, String photoId);

    /**
     * token 换取结果。
     */
    class TokenResult {
        private final String accessToken;
        private final long expiresInSeconds;

        public TokenResult(String accessToken, long expiresInSeconds) {
            this.accessToken = accessToken;
            this.expiresInSeconds = expiresInSeconds;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public long getExpiresInSeconds() {
            return expiresInSeconds;
        }
    }

    /**
     * 单张照片下载结果。
     */
    class DownloadResult {
        private final Status status;
        private final byte[] bytes;

        public DownloadResult(Status status, byte[] bytes) {
            this.status = status;
            this.bytes = bytes;
        }

        public static DownloadResult found(byte[] bytes) {
            return new DownloadResult(Status.FOUND, bytes);
        }

        public static DownloadResult notFound() {
            return new DownloadResult(Status.NOT_FOUND, null);
        }

        public Status getStatus() {
            return status;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public enum Status {
            /** 200，命中图片 */
            FOUND,
            /** 404，缺图，跳过该张不重试 */
            NOT_FOUND
        }
    }

    /**
     * token 无效（401/403），调用方应刷新 token 后重试一次。
     */
    class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }
}
