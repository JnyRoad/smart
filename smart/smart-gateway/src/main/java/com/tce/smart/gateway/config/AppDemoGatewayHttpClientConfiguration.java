package com.tce.smart.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;

/**
 * 隔离本机演示环境的兼容配置。仅当显式开启时，为旧网关依赖组合补充 Netty 客户端；
 * 已有生产 HttpClient Bean 时不会覆盖它。
 */
@Configuration
@ConditionalOnProperty(prefix = "smart.app-demo.gateway", name = "http-client-fallback", havingValue = "true")
public class AppDemoGatewayHttpClientConfiguration {
	@Bean
	@ConditionalOnMissingBean(HttpClient.class)
	public HttpClient appDemoHttpClient() {
		return HttpClient.create();
	}
}
