package com.tce.smart.common.security.feign;

import feign.Feign;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.security.oauth2.client.AccessTokenContextRelay;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

/**
 * feign 配置增强
 *
 */
@Configuration
@ConditionalOnClass(Feign.class)
@EnableConfigurationProperties(SmartInternalServiceTokenProperties.class)
public class SmartFeignConfiguration {

	/**
	 * 使用独立的客户端上下文，避免内部服务令牌与入站用户令牌相互继承。
	 */
	@Bean
	public SmartInternalServiceTokenInterceptor smartInternalServiceTokenInterceptor(
			SmartInternalServiceTokenProperties properties) {
		return new SmartInternalServiceTokenInterceptor(new DefaultOAuth2ClientContext(),
				new SmartInternalServiceTokenResourceFactory().create(properties));
	}

	@Bean
	public RequestInterceptor oauth2FeignRequestInterceptor(ObjectProvider<OAuth2ClientContext> contextProvider,
															ObjectProvider<OAuth2ProtectedResourceDetails> resourceProvider,
															ObjectProvider<AccessTokenContextRelay> relayProvider,
															SmartInternalServiceTokenInterceptor internalServiceTokenInterceptor) {
		OAuth2ClientContext context = contextProvider.getIfAvailable();
		if (context == null) {
			context = new DefaultOAuth2ClientContext();
		}
		OAuth2ProtectedResourceDetails resource = resourceProvider.getIfAvailable();
		if (resource == null) {
			resource = new BaseOAuth2ProtectedResourceDetails();
		}
		AccessTokenContextRelay relay = relayProvider.getIfAvailable();
		if (relay == null) {
			relay = new AccessTokenContextRelay(context);
		}
		return new SmartFeignClientInterceptor(context, resource, relay,
				internalServiceTokenInterceptor);
	}

}
