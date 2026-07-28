package com.tce.smart.data.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;

/** EHR 内部路由按 HandlerMethod 配置精确调用方白名单，支持 Nacos 热更新。 */
@Data
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "security.inner.ehr")
public class EhrInternalRouteClientAccessProperties {

	/**
	 * 以“Controller 全限定类名#方法名”为键；受守卫重载会追加擦除后的参数类型，
	 * 逗号分隔 OAuth client_id 为值的路由白名单。
	 * 未配置或空值均表示拒绝该路由，避免某个服务 client 获得其他 EHR 用途的访问权限。
	 */
	private Map<String, String> routeClientIds = Collections.emptyMap();
}
