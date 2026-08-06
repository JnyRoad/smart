package com.tce.smart.schedule.config;

import com.tce.smart.schedule.security.EnergyProjectionServerTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

/**
 * 能耗投影内部调用使用的 OAuth HTTP 客户端配置。
 */
@Configuration
public class EnergyProjectionOAuthConfiguration {

	private static final int TOKEN_REQUEST_TIMEOUT_MILLIS = 5000;

	/**
	 * 单独创建不经过负载均衡拦截器的短超时客户端，令牌端点由受控配置明确指定。
	 */
	@Bean("energyProjectionOAuthRestOperations")
	public RestOperations energyProjectionOAuthRestOperations() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(TOKEN_REQUEST_TIMEOUT_MILLIS);
		requestFactory.setReadTimeout(TOKEN_REQUEST_TIMEOUT_MILLIS);
		return new RestTemplate(requestFactory);
	}

	/**
	 * 为定时线程提供带缓存的 client_credentials Bearer 令牌。
	 */
	@Bean
	public EnergyProjectionServerTokenProvider energyProjectionServerTokenProvider(
			EnergyProjectionOAuthProperties properties,
			@Qualifier("energyProjectionOAuthRestOperations") RestOperations restOperations) {
		return new EnergyProjectionServerTokenProvider(properties, restOperations, Clock.systemUTC());
	}
}
