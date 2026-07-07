package com.example.demo.pull;

import cn.hutool.core.util.StrUtil;
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

    /**
     * 报错信息里响应体片段的最大长度，防止超长 HTML 错误页刷爆日志。
     */
    private static final int BODY_SNIPPET_MAX_CHARS = 200;

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
            // 注意：不能把 appSecret 打进日志；错误响应体（如 invalid_client）截断后带入报错，便于区分密钥错/网关错
            throw new UnauthorizedException("获取 token 失败，HTTP " + response.getStatus()
                    + "，响应：" + bodySnippet(response));
        }
        JSONObject json = parseJsonBody(response, "token 接口");
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
        JSONObject json = parseJsonBody(response, "待处理清单接口");
        int code = json.getInt("code", -1);
        if (code != 0) {
            throw new IllegalStateException("拉取待处理清单失败，业务码：" + code
                    + "，msg：" + json.getStr("msg", "(无)"));
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
            throw new IllegalStateException("下载照片失败，HTTP " + response.getStatus()
                    + "，响应：" + bodySnippet(response));
        }
        return DownloadResult.found(response.bodyBytes());
    }

    private void assertAuthorized(HttpResponse response) {
        int status = response.getStatus();
        if (status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
            throw new UnauthorizedException("token 无效，HTTP " + status);
        }
    }

    /**
     * 把响应体解析为 JSON；解析失败（如网关误路由返回 HTML 错误页）时带上响应片段抛出，便于定位。
     */
    private JSONObject parseJsonBody(HttpResponse response, String apiName) {
        try {
            return JSONUtil.parseObj(response.body());
        } catch (Exception e) {
            throw new IllegalStateException(apiName + "响应不是合法 JSON，HTTP " + response.getStatus()
                    + "，响应：" + bodySnippet(response), e);
        }
    }

    /**
     * 截断响应体用于报错信息：压掉换行、最多 {@link #BODY_SNIPPET_MAX_CHARS} 字符。
     */
    private String bodySnippet(HttpResponse response) {
        String body = response.body();
        if (StrUtil.isBlank(body)) {
            return "(空响应体)";
        }
        String oneLine = body.replaceAll("\\s+", " ").trim();
        return oneLine.length() <= BODY_SNIPPET_MAX_CHARS
                ? oneLine
                : oneLine.substring(0, BODY_SNIPPET_MAX_CHARS) + "...";
    }
}
