package com.tce.smart.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/** 将统一 API 根路径精确路由到现有认证与平台服务，历史服务前缀路由保持不变。 */
@Configuration
public class ClientApiGatewayConfiguration {
	@Bean
	public RouteLocator clientApiRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
			.route("client-api-v1-session", route -> route.path("/api/v1/sessions")
				.and().method(HttpMethod.POST).uri("lb://smart-auth"))
			.route("client-api-v1-platform", route -> route.path(
				"/api/v1/me",
				"/api/v1/me/apps",
				"/api/v1/item-passes",
				"/api/v1/item-passes/**",
				"/api/v1/visitor-checks",
				"/api/v1/visitor-passes")
				.uri("lb://smart-platform"))
			.build();
	}
}
