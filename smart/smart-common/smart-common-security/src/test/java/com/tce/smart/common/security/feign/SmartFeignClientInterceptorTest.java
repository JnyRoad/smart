package com.tce.smart.common.security.feign;

import feign.RequestTemplate;
import com.tce.smart.common.core.constant.SecurityConstants;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.cloud.security.oauth2.client.AccessTokenContextRelay;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * 内部 Feign 在没有入站 HTTP 请求（定时任务、异步任务）时仍必须携带服务令牌。
 */
public class SmartFeignClientInterceptorTest {

	@After
	public void clearRequestContext() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	public void noRequestContextReplacesResidualUserAuthorizationWithServiceToken() {
		OAuth2ClientContext context = contextWithResidualUserToken();
		RequestTemplate template = serviceTemplate();
		template.header("Authorization", "Bearer end-user-token");

		interceptor(context).apply(template);

		assertOnlyServiceAuthorization(template);
		Mockito.verify(context).setAccessToken(Mockito.isNull(OAuth2AccessToken.class));
		assertEquals("service-token", context.getAccessToken().getValue());
	}

	@Test
	public void inboundUserAuthorizationIsNotForwardedToServiceTokenEndpoint() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer end-user-token");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		OAuth2ClientContext context = contextWithResidualUserToken();
		RequestTemplate template = serviceTemplate();

		interceptor(context).apply(template);

		assertOnlyServiceAuthorization(template);
		Mockito.verify(context).setAccessToken(Mockito.isNull(OAuth2AccessToken.class));
		assertEquals("service-token", context.getAccessToken().getValue());
	}

	private OAuth2ClientContext contextWithResidualUserToken() {
		return Mockito.spy(new DefaultOAuth2ClientContext(new DefaultOAuth2AccessToken("end-user-token")));
	}

	private SmartFeignClientInterceptor interceptor(OAuth2ClientContext context) {
		return new ServiceTokenInterceptor(
				context,
				Mockito.mock(OAuth2ProtectedResourceDetails.class),
				Mockito.mock(AccessTokenContextRelay.class));
	}

	private RequestTemplate serviceTemplate() {
		RequestTemplate template = new RequestTemplate();
		template.header(SecurityConstants.INTERNAL_SERVICE_AUTH, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		return template;
	}

	private void assertOnlyServiceAuthorization(RequestTemplate template) {
		Collection<String> authorizations = template.headers().get("Authorization");
		assertEquals(1, authorizations.size());
		assertEquals("Bearer service-token", authorizations.iterator().next());
	}

	/** 测试替身模拟 client_credentials 获取成功，并把服务令牌写回 OAuth 上下文。 */
	private static class ServiceTokenInterceptor extends SmartFeignClientInterceptor {
		private final OAuth2ClientContext context;

		private ServiceTokenInterceptor(OAuth2ClientContext context, OAuth2ProtectedResourceDetails resource,
				AccessTokenContextRelay accessTokenContextRelay) {
			super(context, resource, accessTokenContextRelay);
			this.context = context;
		}

		@Override
		public OAuth2AccessToken getToken() {
			OAuth2AccessToken serviceToken = new DefaultOAuth2AccessToken("service-token");
			context.setAccessToken(serviceToken);
			return serviceToken;
		}
	}
}
