package com.tce.smart.common.security.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import feign.RequestTemplate;
import org.junit.Test;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * 内部敏感 Feign 必须使用独立服务资源，且取令牌失败时不能继续向下游发送请求。
 */
public class SmartInternalServiceTokenInterceptorTest {

	@Test
	public void serviceResourceAlwaysUsesClientCredentialsAndServerScope() {
		SmartInternalServiceTokenProperties properties = configuredProperties();
		ClientCredentialsResourceDetails resource = new SmartInternalServiceTokenResourceFactory().create(properties);

		assertEquals(SecurityConstants.CLIENT_CREDENTIALS, resource.getGrantType());
		assertEquals(Collections.singletonList("server"), resource.getScope());
		assertEquals("internal-service", resource.getId());
	}

	@Test
	public void tokenAcquisitionFailureStopsBeforeAuthorizationCanBeSent() {
		RequestTemplate template = new RequestTemplate();
		template.header("Authorization", "Bearer end-user-token");
		SmartInternalServiceTokenInterceptor interceptor = new FailingServiceTokenInterceptor(
				new DefaultOAuth2ClientContext(),
				new SmartInternalServiceTokenResourceFactory().create(configuredProperties()));

		try {
			interceptor.apply(template);
			fail("服务令牌获取失败必须中断 Feign 调用");
		} catch (OAuth2AccessDeniedException expected) {
			assertNull("失败时不得保留可发送到下游的 Authorization", template.headers().get("Authorization"));
		}
	}

	private SmartInternalServiceTokenProperties configuredProperties() {
		SmartInternalServiceTokenProperties properties = new SmartInternalServiceTokenProperties();
		properties.setClientId("internal-client");
		properties.setClientSecret("internal-secret");
		properties.setAccessTokenUri("http://smart-auth:3000/oauth/token");
		return properties;
	}

	/** 模拟授权服务器拒绝客户端凭据，确保异常不会被降级为匿名调用。 */
	private static class FailingServiceTokenInterceptor extends SmartInternalServiceTokenInterceptor {
		private FailingServiceTokenInterceptor(DefaultOAuth2ClientContext context,
				ClientCredentialsResourceDetails resource) {
			super(context, resource);
		}

		@Override
		public org.springframework.security.oauth2.common.OAuth2AccessToken getToken() {
			throw new OAuth2AccessDeniedException("client_credentials denied");
		}
	}
}
