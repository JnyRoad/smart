package com.tce.smart.data.security;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiInterceptor;
import com.tce.smart.data.controller.ehrview.EvwEmphrYsController;
import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.web.method.HandlerMethod;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * EHR 内部路由必须在统一入口按精确服务 client_id 收口，不能只依赖任意 server scope。
 */
public class EhrInternalRouteClientAccessInterceptorTest {

	private static final String ALLOWED_CLIENT = "smart-app";
	private static final Pattern INNER_SERVER_ROUTE = Pattern.compile(
			"(?m)^\\s*@Inner\\s*\\R\\s*@OpenApi\\(\\\"server\\\"\\)\\s*\\R\\s*@(Get|Post|Put|Delete|Patch)Mapping\\(");

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void everyRealDataInnerServerRouteUsesTheUnifiedExactClientGuard() throws Exception {
		EhrInternalRouteClientAccessInterceptor interceptor = interceptor();
		Method representativeRoute = EvwEmphrYsController.class.getMethod("info", String.class);
		assertTrue("统一守卫必须按 @Inner + @OpenApi(server) 识别路由",
				interceptor.requiresExactClientGuard(representativeRoute));
		assertTrue("测试必须从真实 Controller 源码枚举到内部服务路由", countRealInnerServerRoutes() > 0);
	}

	@Test
	public void accessMatrixRejectsEveryBypassAndAllowsConfiguredServiceClient() throws Exception {
		EhrInternalRouteClientAccessInterceptor interceptor = interceptor();
		OpenApiInterceptor openApiInterceptor = new OpenApiInterceptor(new OpenApiAuthenticationAdapter());
		HandlerMethod handler = new HandlerMethod(new EvwEmphrYsController(),
				EvwEmphrYsController.class.getMethod("info", String.class));

		assertDeniedByExactGuard(interceptor, handler, null, SecurityConstants.FROM_IN);
		assertDeniedByExactGuard(interceptor, handler, new TestingAuthenticationToken("user", "credentials"),
				SecurityConstants.FROM_IN);
		SecurityContextHolder.getContext().setAuthentication(serverAuthentication("other-scope", ALLOWED_CLIENT));
		assertFalse("错误 scope 必须由统一 OpenApi 守卫拒绝",
				openApiInterceptor.preHandle(request(SecurityConstants.FROM_IN), new MockHttpServletResponse(), handler));
		assertDeniedByExactGuard(interceptor, handler, serverAuthentication("server", "unexpected-client"),
				SecurityConstants.FROM_IN);
		assertDeniedByExactGuard(interceptor, handler, serverAuthentication("server", ALLOWED_CLIENT), null);

		SecurityContextHolder.getContext().setAuthentication(serverAuthentication("server", ALLOWED_CLIENT));
		MockHttpServletRequest request = request(SecurityConstants.FROM_IN);
		assertTrue("配置白名单中的 server client 且带内部来源头必须通过 OpenApi 守卫",
				openApiInterceptor.preHandle(request, new MockHttpServletResponse(), handler));
		assertTrue("配置白名单中的 server client 且带内部来源头必须通过精确 client_id 守卫",
				interceptor.preHandle(request, new MockHttpServletResponse(), handler));
	}

	@Test
	public void blankAllowlistFailsClosed() throws Exception {
		EhrInternalRouteClientAccessInterceptor interceptor = new EhrInternalRouteClientAccessInterceptor(
				new OpenApiAuthenticationAdapter(), properties(""));
		HandlerMethod handler = new HandlerMethod(new EvwEmphrYsController(),
				EvwEmphrYsController.class.getMethod("info", String.class));
		assertDeniedByExactGuard(interceptor, handler, serverAuthentication("server", ALLOWED_CLIENT),
				SecurityConstants.FROM_IN);
	}

	@Test
	public void refreshedAllowlistTakesEffectWithoutRestartingTheGuard() throws Exception {
		EhrInternalRouteClientAccessProperties properties = properties("");
		EhrInternalRouteClientAccessInterceptor interceptor = new EhrInternalRouteClientAccessInterceptor(
				new OpenApiAuthenticationAdapter(), properties);
		HandlerMethod handler = new HandlerMethod(new EvwEmphrYsController(),
				EvwEmphrYsController.class.getMethod("info", String.class));

		assertDeniedByExactGuard(interceptor, handler, serverAuthentication("server", ALLOWED_CLIENT),
				SecurityConstants.FROM_IN);
		properties.setAllowedClientIds(ALLOWED_CLIENT);
		SecurityContextHolder.getContext().setAuthentication(serverAuthentication("server", ALLOWED_CLIENT));
		assertTrue("刷新后的白名单必须立即被守卫使用",
				interceptor.preHandle(request(SecurityConstants.FROM_IN), new MockHttpServletResponse(), handler));
		properties.setAllowedClientIds("");
		assertDeniedByExactGuard(interceptor, handler, serverAuthentication("server", ALLOWED_CLIENT),
				SecurityConstants.FROM_IN);
		assertTrue("白名单属性必须是 Nacos 可刷新配置",
				EhrInternalRouteClientAccessProperties.class.isAnnotationPresent(RefreshScope.class));
	}

	@Test
	public void nacosTemplateUsesAnEmptyDefaultAllowlist() throws IOException {
		Path template = locateRepositoryRoot().resolve("docker/nacos/config/dev/smart-data.yml");
		String source = new String(Files.readAllBytes(template), java.nio.charset.StandardCharsets.UTF_8);
		assertTrue("Nacos 模板必须由环境变量显式注入允许的 EHR 调用方",
				source.contains("allowed-client-ids: \"${SMART_DATA_EHR_ALLOWED_CLIENT_IDS:}\""));
	}

	private EhrInternalRouteClientAccessInterceptor interceptor() {
		return new EhrInternalRouteClientAccessInterceptor(new OpenApiAuthenticationAdapter(),
				properties(ALLOWED_CLIENT + ", smart-platform, smart-upms"));
	}

	private EhrInternalRouteClientAccessProperties properties(String allowedClientIds) {
		EhrInternalRouteClientAccessProperties properties = new EhrInternalRouteClientAccessProperties();
		properties.setAllowedClientIds(allowedClientIds);
		return properties;
	}

	private void assertDeniedByExactGuard(EhrInternalRouteClientAccessInterceptor interceptor, HandlerMethod handler,
			org.springframework.security.core.Authentication authentication, String from) throws Exception {
		SecurityContextHolder.getContext().setAuthentication(authentication);
		MockHttpServletResponse response = new MockHttpServletResponse();
		assertFalse(interceptor.preHandle(request(from), response, handler));
		assertTrue("精确 client_id 守卫拒绝时必须返回 403", response.getStatus() == 403);
	}

	private MockHttpServletRequest request(String from) {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/emphr/ys/info");
		if (from != null) {
			request.addHeader(SecurityConstants.FROM, from);
		}
		return request;
	}

	private OAuth2Authentication serverAuthentication(String scope, String clientId) {
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), clientId, Collections.emptyList(), true,
				Collections.singleton(scope), Collections.emptySet(), null, Collections.emptySet(), Collections.emptyMap());
		return new OAuth2Authentication(request, null);
	}

	private int countRealInnerServerRoutes() throws IOException {
		Path controllerRoot = locateRepositoryRoot().resolve(
				"smart-module/smart-data/smart-data-biz/src/main/java/com/tce/smart/data/controller");
		try (Stream<Path> paths = Files.walk(controllerRoot)) {
			int routeCount = 0;
			for (Path controller : paths.filter(path -> path.toString().endsWith("Controller.java"))
					.collect(Collectors.toList())) {
				String source = new String(Files.readAllBytes(controller), java.nio.charset.StandardCharsets.UTF_8);
				Matcher matcher = INNER_SERVER_ROUTE.matcher(source.replace("\r\n", "\n"));
				while (matcher.find()) {
					routeCount++;
				}
			}
			return routeCount;
		}
	}

	private Path locateRepositoryRoot() {
		Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		while (current != null) {
			if (Files.isRegularFile(current.resolve("docker/nacos/config/dev/smart-data.yml"))) {
				return current;
			}
			current = current.getParent();
		}
		throw new IllegalStateException("无法定位仓库根目录");
	}
}
