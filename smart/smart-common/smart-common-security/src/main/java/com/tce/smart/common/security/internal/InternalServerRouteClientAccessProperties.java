package com.tce.smart.common.security.internal;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/** Bridge 系列内部路由精确调用方白名单，支持 Nacos 热更新。 */
@Data
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "security.inner.bridge")
public class InternalServerRouteClientAccessProperties {

	/** 逗号分隔的 OAuth client_id 白名单；空值表示拒绝全部内部服务调用。 */
	private String allowedClientIds = "";
}
