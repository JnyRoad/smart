package com.tce.smart.common.security.feign;

import feign.RequestTemplate;
import com.tce.smart.common.core.constant.SecurityConstants;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.cloud.security.oauth2.client.AccessTokenContextRelay;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.web.context.request.RequestContextHolder;

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
	public void noRequestContextStillAddsBearerToken() {
		OAuth2ClientContext context = Mockito.mock(OAuth2ClientContext.class);
		Mockito.when(context.getAccessToken()).thenReturn(new DefaultOAuth2AccessToken("service-token"));
		SmartFeignClientInterceptor interceptor = new SmartFeignClientInterceptor(
				context,
				Mockito.mock(OAuth2ProtectedResourceDetails.class),
				Mockito.mock(AccessTokenContextRelay.class));
		RequestTemplate template = new RequestTemplate();
		template.header(SecurityConstants.INTERNAL_SERVICE_AUTH, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);

		interceptor.apply(template);

		assertEquals("Bearer service-token", template.headers().get("Authorization").iterator().next());
	}
}
