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
import static org.junit.Assert.fail;

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

		/** 提供兼容历史能耗权限的 server 空入口，仅供反射读取授权注解，不执行业务。 */
		@OpenApi(value = "server", compatibilityScopes = {"internal:energy:projection:run"})
		public void migrationHandlerMethod() {
		}

		/** 提供含未登记兼容权限的空入口，用于验证注解不能绕过目录校验，不执行业务。 */
		@OpenApi(value = "server", compatibilityScopes = {"internal:unknown:scope"})
		public void invalidMigrationHandlerMethod() {
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

	private HandlerMethod migrationHandler() throws NoSuchMethodException {
		Method method = SampleController.class.getMethod("migrationHandlerMethod");
		return new HandlerMethod(new SampleController(), method);
	}

	private HandlerMethod invalidMigrationHandler() throws NoSuchMethodException {
		Method method = SampleController.class.getMethod("invalidMigrationHandlerMethod");
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

	/** 验证未登记的 scope 即使写入兼容注解也返回 403，防止未知授权被误放行。 */
	@Test
	public void migrationDoesNotTreatUnknownScopeAsCompatibilityScope() throws Exception {
		Set<String> unregisteredScope = new HashSet<>();
		unregisteredScope.add("internal:unknown:scope");
		SecurityContextHolder.getContext().setAuthentication(clientOnlyAuthentication("unknown-scope-client", unregisteredScope));

		MockHttpServletResponse response = new MockHttpServletResponse();
		assertFalse(interceptor.preHandle(new MockHttpServletRequest(), response, invalidMigrationHandler()));
		assertEquals(403, response.getStatus());
	}

	/** 验证关闭历史兼容后 server 仍获放行，避免迁移开关误禁用当前主授权。 */
	@Test
	public void primaryServerScopeRemainsAllowedWhenCompatibilityIsDisabled() throws Exception {
		Set<String> serverScope = new HashSet<>();
		serverScope.add("server");
		SecurityContextHolder.getContext().setAuthentication(clientOnlyAuthentication("server-client", serverScope));
		OpenApiInterceptor cutoverInterceptor = new OpenApiInterceptor(adapter, false);

		MockHttpServletResponse response = new MockHttpServletResponse();
		assertTrue(cutoverInterceptor.preHandle(new MockHttpServletRequest(), response, migrationHandler()));
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

	/** 验证 server 与明确声明的历史能耗权限可用，同时拒绝仅名称相近的 server:read。 */
	@Test
	public void migrationScopeAcceptsPrimaryServerOrExplicitHistoricalScope() throws Exception {
		Set<String> dedicatedScope = new HashSet<>();
		dedicatedScope.add("internal:energy:projection:run");
		SecurityContextHolder.getContext().setAuthentication(clientOnlyAuthentication("smart-schedule", dedicatedScope));
		assertTrue(interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), migrationHandler()));

		SecurityContextHolder.clearContext();
		Set<String> serverScope = new HashSet<>();
		serverScope.add("server");
		SecurityContextHolder.getContext().setAuthentication(clientOnlyAuthentication("server-schedule", serverScope));
		assertTrue(interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), migrationHandler()));

		SecurityContextHolder.clearContext();
		Set<String> unrelatedScope = new HashSet<>();
		unrelatedScope.add("server:read");
		SecurityContextHolder.getContext().setAuthentication(clientOnlyAuthentication("other-service", unrelatedScope));
		MockHttpServletResponse response = new MockHttpServletResponse();
		assertFalse(interceptor.preHandle(new MockHttpServletRequest(), response, migrationHandler()));
		assertEquals(403, response.getStatus());
	}

	/**
	 * 迁移期需要临时接受旧 scope，但主 scope 仍必须保持单值，避免把所有新接口退化为通配授权。
	 */
	@Test
	public void openApiDeclaresEmptyCompatibilityScopesByDefault() {
		try {
			Method method = OpenApi.class.getMethod("compatibilityScopes");
			assertEquals(0, ((String[]) method.getDefaultValue()).length);
		} catch (NoSuchMethodException e) {
			fail("@OpenApi 必须声明仅用于迁移的 compatibilityScopes 属性");
		}
	}
}
