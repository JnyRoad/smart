package com.tce.smart.push.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/29 .
 * @Modified By:
 */
@Component
@Configuration
public class CommonProperties {

    @Value("${uni.push.app-id}")
	private String appId;
	@Value("${uni.push.app-key}")
	private String appKey;
	@Value("${uni.push.master-secret}")
	private String masterSecret;
    @Value("${uni.push.url}")
    private String url;
    @Value("${uni.push.offline-expiretime}")
    private long offlineExpireTime;
    public String getAppId() {
        return appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getMasterSecret() {
        return masterSecret;
    }

    public String getUrl() {
        return url;
    }

    public long getOfflineExpireTime() {
        return offlineExpireTime;
    }
}
