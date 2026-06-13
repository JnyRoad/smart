package com.tce.smart.common.security.social;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信登录配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "social.wx")
public class WxSocialConfig {
	private String appid;
	private String secret;
}
