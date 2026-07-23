package com.tce.smart.common.security.feign;

import feign.RequestTemplate;
import feign.RequestInterceptor;
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
		Mockito.verify(context, Mockito.never()).setAccessToken(Mockito.any(OAuth2AccessToken.class));
		assertEquals("end-user-token", context.getAccessToken().getValue());
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
		Mockito.verify(context, Mockito.never()).setAccessToken(Mockito.any(OAuth2AccessToken.class));
		assertEquals("end-user-token", context.getAccessToken().getValue());
	}

	private OAuth2ClientContext contextWithResidualUserToken() {
		return Mockito.spy(new DefaultOAuth2ClientContext(new DefaultOAuth2AccessToken("end-user-token")));
	}

	private SmartFeignClientInterceptor interceptor(OAuth2ClientContext context) {
		return new SmartFeignClientInterceptor(
				context,
				Mockito.mock(OAuth2ProtectedResourceDetails.class),
				Mockito.mock(AccessTokenContextRelay.class),
				new ServiceTokenInterceptor());
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

	/** 测试替身模拟独立 client_credentials 服务资源成功写入服务令牌。 */
	private static class ServiceTokenInterceptor implements RequestInterceptor {
		@Override
		public void apply(RequestTemplate template) {
			template.header("Authorization", "Bearer service-token");
		}
	}
}
