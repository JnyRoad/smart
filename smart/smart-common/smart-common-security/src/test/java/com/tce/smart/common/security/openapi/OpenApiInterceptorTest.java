package com.tce.smart.common.security.openapi;

import com.tce.smart.common.security.annotation.OpenApi;
import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * OpenApiInterceptor 裁决矩阵单测：覆盖简报要求的 6 个用例，
 * 校验 preHandle 对 @OpenApi 标注接口 / 普通接口在客户端 token、用户 token、匿名场景下的放行 / 拒绝行为。
 * <p>
 * 拒绝场景断言 {@code preHandle} 返回 {@code false} 且响应状态码为 403——不再依赖抛异常，
 * 因为本仓库的异常翻译链够不着拦截器阶段（详见 {@link OpenApiInterceptor} 类注释）。
 */
public class OpenApiInterceptorTest {

	private final OpenApiAuthenticationAdapter adapter = new OpenApiAuthenticationAdapter();
	private final OpenApiInterceptor interceptor = new OpenApiInterceptor(adapter);

	/** 测试专用 Controller：一个标注 @OpenApi，一个不标注（模拟普通接口）。 */
	static class SampleController {
		@OpenApi("park:read")
		public void openApiHandlerMethod() {
		}

		public void plainHandlerMethod() {
		}
	}

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	private HandlerMethod openApiHandler() throws NoSuchMethodException {
		Method method = SampleController.class.getMethod("openApiHandlerMethod");
		return new HandlerMethod(new SampleController(), method);
	}

	private HandlerMethod plainHandler() throws NoSuchMethodException {
		Method method = SampleController.class.getMethod("plainHandlerMethod");
		return new HandlerMethod(new SampleController(), method);
	}

	/** 构造 client_credentials 模式的 OAuth2Authentication，isClientOnly()=true，携带给定 scope。 */
	private OAuth2Authentication clientOnlyAuthentication(String clientId, Set<String> scopes) {
		OAuth2Request oAuth2Request = new OAuth2Request(
				Collections.emptyMap(), clientId, Collections.emptyList(),
				true, scopes, Collections.emptySet(),
				null, Collections.emptySet(), Collections.emptyMap());
		// 第二个参数（userAuthentication）为 null 时 OAuth2Authentication.isClientOnly() 返回 true
		return new OAuth2Authentication(oAuth2Request, null);
	}

	/** 构造用户 token 场景的 Authentication（非 OAuth2Authentication，模拟资源服务解析出的用户认证）。 */
	private Authentication userAuthentication() {
		return new UsernamePasswordAuthenticationToken("normal-user", "N/A", Collections.emptyList());
	}

	@Test
	public void clientToken_withScope_onOpenApi_passes() throws Exception {
		Set<String> scopes = new HashSet<>();
		scopes.add("park:read");
		SecurityContextHolder.getContext().setAuthentication(clientOnlyAuthentication("open-app", scopes));

		boolean result = interceptor.preHandle(
				new MockHttpServletRequest("GET", "/open/park"), new MockHttpServletResponse(), openApiHandler());

		assertTrue(result);
	}

	@Test
	public void clientToken_missingScope_onOpenApi_403() throws Exception {
		Set<String> scopes = new HashSet<>();
		scopes.add("other:scope");
		SecurityContextHolder.getContext().setAuthentication(clientOnlyAuthentication("open-app", scopes));

		MockHttpServletResponse response = new MockHttpServletResponse();
		boolean result = interceptor.preHandle(
				new MockHttpServletRequest("GET", "/open/park"), response, openApiHandler());

		assertFalse(result);
		assertEquals(403, response.getStatus());
	}

	@Test
	public void userToken_onOpenApi_403() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(userAuthentication());

		MockHttpServletResponse response = new MockHttpServletResponse();
		boolean result = interceptor.preHandle(
				new MockHttpServletRequest("GET", "/open/park"), response, openApiHandler());

		assertFalse(result);
		assertEquals(403, response.getStatus());
	}

	@Test
	public void clientToken_onPlainEndpoint_403() throws Exception {
		// deny-by-default（Codex 阻断项回归）：client_credentials token 不允许访问未标注 @OpenApi 的普通接口
		Set<String> scopes = new HashSet<>();
		scopes.add("park:read");
		SecurityContextHolder.getContext().setAuthentication(clientOnlyAuthentication("open-app", scopes));

		MockHttpServletResponse response = new MockHttpServletResponse();
		boolean result = interceptor.preHandle(
				new MockHttpServletRequest("GET", "/plain"), response, plainHandler());

		assertFalse(result);
		assertEquals(403, response.getStatus());
	}

	@Test
	public void userToken_onPlainEndpoint_passes() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(userAuthentication());

		boolean result = interceptor.preHandle(
				new MockHttpServletRequest("GET", "/plain"), new MockHttpServletResponse(), plainHandler());

		assertTrue(result);
	}

	@Test
	public void anonymous_onOpenApi_403() throws Exception {
		// SecurityContext 未注入任何 Authentication，模拟匿名访问
		MockHttpServletResponse response = new MockHttpServletResponse();
		boolean result = interceptor.preHandle(
				new MockHttpServletRequest("GET", "/open/park"), response, openApiHandler());

		assertFalse(result);
		assertEquals(403, response.getStatus());
	}
}
