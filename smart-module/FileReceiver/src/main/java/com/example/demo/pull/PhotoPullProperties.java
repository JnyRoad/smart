package com.example.demo.pull;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 照片拉取相关配置。
 * <p>
 * 对应配置前缀 file-receiver.pull.*，以及独立的 photo-dir / cleanup 配置组。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file-receiver")
public class PhotoPullProperties {

    /**
     * 拉取子配置组：file-receiver.pull.*
     */
    private Pull pull = new Pull();

    /**
     * 本地照片存放目录，默认 D:/visitor（许昌打印机 Windows 机部署路径）。
     */
    private String photoDir = "D:/visitor";

    /**
     * 过期清理子配置组：file-receiver.cleanup.*
     */
    private Cleanup cleanup = new Cleanup();

    @Data
    public static class Pull {
        /**
         * 是否启用拉取任务，默认关闭，避免未配置服务端信息时误跑。
         */
        private boolean enabled = false;

        /**
         * 网关地址，例如 http://gateway-host:port，/auth 与 /platform 为网关路由前缀。
         */
        private String serverUrl;

        /**
         * OAuth2 client_credentials 的 app-id（Basic 认证用户名）。
         */
        private String appId;

        /**
         * OAuth2 client_credentials 的 app-secret（Basic 认证密码，禁止打日志）。
         */
        private String appSecret;

        /**
         * 拉取轮询间隔（秒），默认 30。
         */
        private int intervalSeconds = 30;
    }

    @Data
    public static class Cleanup {
        /**
         * 本地照片保留天数，超过则视为过期候选；0 表示关闭清理任务。
         */
        private int retentionDays = 7;
    }
}
