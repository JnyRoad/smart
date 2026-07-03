package com.example.demo.pull;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Hutool HttpUtil 的照片服务端客户端实现。
 * <p>
 * 网关地址 server-url 下，/auth 与 /platform 是网关路由前缀。
 */
@Slf4j
@Component
public class HutoolPhotoServerClient implements PhotoServerClient {

    /**
     * 连接超时：5s。
     */
    private static final int CONNECT_TIMEOUT_MILLIS = 5_000;

    /**
     * 读超时：30s（清单/下载接口用）。
     */
    private static final int READ_TIMEOUT_MILLIS = 30_000;

    /**
     * token 接口读超时单独取 5s，避免鉴权慢拖累整轮。
     */
    private static final int TOKEN_READ_TIMEOUT_MILLIS = 5_000;

    @Value("${file-receiver.pull.server-url:}")
    private String serverUrl;

    @Override
    public TokenResult fetchToken(String appId, String appSecret) {
        String url = serverUrl + "/auth/oauth/token?grant_type=client_credentials";
        HttpResponse response = HttpRequest.post(url)
                .basicAuth(appId, appSecret)
                .setConnectionTimeout(CONNECT_TIMEOUT_MILLIS)
                .setReadTimeout(TOKEN_READ_TIMEOUT_MILLIS)
                .execute();
        if (!response.isOk()) {
            // 注意：不能把 appSecret 打进日志，这里只记录状态码
            throw new UnauthorizedException("获取 token 失败，HTTP " + response.getStatus());
        }
        JSONObject json = JSONUtil.parseObj(response.body());
        String accessToken = json.getStr("access_token");
        long expiresIn = json.getLong("expires_in", 0L);
        return new TokenResult(accessToken, expiresIn);
    }

    @Override
    public List<String> fetchPendingPhotoIds(String accessToken) {
        String url = serverUrl + "/platform/open/admittance/photo/pending";
        HttpResponse response = HttpRequest.get(url)
                .header("Authorization", "Bearer " + accessToken)
                .setConnectionTimeout(CONNECT_TIMEOUT_MILLIS)
                .setReadTimeout(READ_TIMEOUT_MILLIS)
                .execute();
        assertAuthorized(response);
        JSONObject json = JSONUtil.parseObj(response.body());
        int code = json.getInt("code", -1);
        if (code != 0) {
            throw new IllegalStateException("拉取待处理清单失败，业务码：" + code);
        }
        JSONArray data = json.getJSONArray("data");
        List<String> photoIds = new ArrayList<>();
        if (data != null) {
            for (Object item : data) {
                photoIds.add(String.valueOf(item));
            }
        }
        return photoIds;
    }

    @Override
    public DownloadResult downloadPhoto(String accessToken, String photoId) {
        String url = serverUrl + "/platform/open/admittance/photo/download/" + photoId;
        HttpResponse response = HttpUtil.createGet(url)
                .header("Authorization", "Bearer " + accessToken)
                .setConnectionTimeout(CONNECT_TIMEOUT_MILLIS)
                .setReadTimeout(READ_TIMEOUT_MILLIS)
                .execute();
        if (response.getStatus() == HttpStatus.NOT_FOUND.value()) {
            return DownloadResult.notFound();
        }
        assertAuthorized(response);
        if (!response.isOk()) {
            throw new IllegalStateException("下载照片失败，HTTP " + response.getStatus());
        }
        return DownloadResult.found(response.bodyBytes());
    }

    private void assertAuthorized(HttpResponse response) {
        int status = response.getStatus();
        if (status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
            throw new UnauthorizedException("token 无效，HTTP " + status);
        }
    }
}
