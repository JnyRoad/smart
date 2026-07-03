package com.example.demo.pull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * OAuth2 client_credentials 模式的 token 客户端。
 * <p>
 * 内存缓存 token，提前 60s 视为过期以规避临界点请求刚好用到失效 token。
 */
@Slf4j
@Component
public class OpenApiTokenClient {

    /**
     * 提前刷新窗口：60s。
     */
    private static final long REFRESH_MARGIN_MILLIS = 60_000L;

    private final PhotoServerClient serverClient;
    private final PhotoPullProperties properties;

    private volatile String cachedToken;
    private volatile long expireAtEpochMillis;

    @Autowired
    public OpenApiTokenClient(PhotoServerClient serverClient, PhotoPullProperties properties) {
        this.serverClient = serverClient;
        this.properties = properties;
    }

    /**
     * 获取可用 token，命中缓存则直接返回，否则换取新 token。
     */
    public String getToken() {
        if (cachedToken != null && System.currentTimeMillis() < expireAtEpochMillis) {
            return cachedToken;
        }
        return refresh();
    }

    /**
     * 强制刷新 token（401 场景下调用方应调用本方法后重试一次）。
     */
    public String refresh() {
        PhotoPullProperties.Pull pull = properties.getPull();
        PhotoServerClient.TokenResult tokenResult = serverClient.fetchToken(pull.getAppId(), pull.getAppSecret());
        this.cachedToken = tokenResult.getAccessToken();
        this.expireAtEpochMillis = System.currentTimeMillis()
                + tokenResult.getExpiresInSeconds() * 1000L
                - REFRESH_MARGIN_MILLIS;
        log.info("已刷新 access token，有效期 {} 秒", tokenResult.getExpiresInSeconds());
        return cachedToken;
    }
}
