package com.tce.smart.bridge.security;

import com.tce.smart.bridge.controller.BridgeController;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.internal.InternalServerRouteClientAccessInterceptor;
import com.tce.smart.common.security.internal.InternalServerRouteClientAccessProperties;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiInterceptor;
import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.web.method.HandlerMethod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Bridge 内部路由除 server scope 外，还必须收口至 Nacos 明确授权的调用服务 client_id。
 */
public class BridgeInternalRouteClientAccessInterceptorTest {

	private static final String DISPATCHER_CLIENT = "dispatcher-client";
	private static final String OTHER_SERVER_CLIENT = "another-server-client";

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void dispatcherClientWithInternalSourceIsAllowed() throws Exception {
		InternalServerRouteClientAccessInterceptor interceptor = interceptor(DISPATCHER_CLIENT);
		HandlerMethod handler = bridgeHandler("dispatch");
		OpenApiInterceptor openApiInterceptor = new OpenApiInterceptor(new OpenApiAuthenticationAdapter());
		SecurityContextHolder.getContext().setAuthentication(serverAuthentication(DISPATCHER_CLIENT));

		MockHttpServletRequest request = request(SecurityConstants.FROM_IN);
		assertTrue("dispatcher 的 server scope 必须先通过通用 OpenApi 守卫",
				openApiInterceptor.preHandle(request, new MockHttpServletResponse(), handler));
		assertTrue("白名单中的 dispatcher client 且带内部来源头必须通过 Bridge 精确守卫",
				interceptor.preHandle(request, new MockHttpServletResponse(), handler));
	}

	@Test
	public void otherServerClientIsDeniedEvenWhenItsScopeIsServer() throws Exception {
		InternalServerRouteClientAccessInterceptor interceptor = interceptor(DISPATCHER_CLIENT);
		HandlerMethod handler = bridgeHandler("getImage");
		OpenApiInterceptor openApiInterceptor = new OpenApiInterceptor(new OpenApiAuthenticationAdapter());
		SecurityContextHolder.getContext().setAuthentication(serverAuthentication(OTHER_SERVER_CLIENT));

		assertTrue("现有通用守卫会接受任意携带 server scope 的服务令牌，必须由 Bridge 守卫继续收口",
				openApiInterceptor.preHandle(request(SecurityConstants.FROM_IN), new MockHttpServletResponse(), handler));
		assertDenied(interceptor, handler, SecurityConstants.FROM_IN);
	}

	@Test
	public void blankAllowlistFailsClosed() throws Exception {
		InternalServerRouteClientAccessInterceptor interceptor = interceptor("");
		HandlerMethod handler = bridgeHandler("getThumbnail");
		SecurityContextHolder.getContext().setAuthentication(serverAuthentication(DISPATCHER_CLIENT));

		assertDenied(interceptor, handler, SecurityConstants.FROM_IN);
	}

	@Test
	public void wrongOrMissingFromHeaderIsDenied() throws Exception {
		InternalServerRouteClientAccessInterceptor interceptor = interceptor(DISPATCHER_CLIENT);
		HandlerMethod handler = bridgeHandler("dispatch");
		SecurityContextHolder.getContext().setAuthentication(serverAuthentication(DISPATCHER_CLIENT));

		assertDenied(interceptor, handler, "N");
		assertDenied(interceptor, handler, null);
	}

	@Test
	public void nacosBridgeTemplatesDefaultToAnEmptyAllowlist() throws IOException {
		List<Path> templates = bridgeNacosTemplates();
		assertFalse("测试必须枚举到至少一个 Bridge Nacos 模板", templates.isEmpty());
		for (Path template : templates) {
			String source = new String(Files.readAllBytes(template), StandardCharsets.UTF_8);
			assertTrue(template + " 必须由环境变量显式注入 Bridge 允许调用方",
					source.contains("allowed-client-ids: \"${SMART_BRIDGE_ALLOWED_CLIENT_IDS:}\""));
		}
	}

	private InternalServerRouteClientAccessInterceptor interceptor(String allowedClientIds) {
		InternalServerRouteClientAccessProperties properties = new InternalServerRouteClientAccessProperties();
		properties.setAllowedClientIds(allowedClientIds);
		return new InternalServerRouteClientAccessInterceptor(new OpenApiAuthenticationAdapter(), properties);
	}

	private HandlerMethod bridgeHandler(String methodName) throws Exception {
		Method method = Stream.of(BridgeController.class.getDeclaredMethods())
				.filter(candidate -> methodName.equals(candidate.getName()))
				.findFirst()
				.orElseThrow(() -> new AssertionError("缺少 Bridge 路由方法 " + methodName));
		return new HandlerMethod(new BridgeController(null, null), method);
	}

	private void assertDenied(InternalServerRouteClientAccessInterceptor interceptor, HandlerMethod handler,
			String from) throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		assertFalse(interceptor.preHandle(request(from), response, handler));
		assertTrue("精确 client_id 守卫拒绝时必须返回 403", response.getStatus() == 403);
	}

	private MockHttpServletRequest request(String from) {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/bridge/dispatch");
		if (from != null) {
			request.addHeader(SecurityConstants.FROM, from);
		}
		return request;
	}

	private OAuth2Authentication serverAuthentication(String clientId) {
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), clientId, Collections.emptyList(), true,
				Collections.singleton("server"), Collections.emptySet(), null, Collections.emptySet(),
				Collections.emptyMap());
		return new OAuth2Authentication(request, null);
	}

	private List<Path> bridgeNacosTemplates() throws IOException {
		Path configRoot = locateRepositoryRoot().resolve("docker/nacos/config/dev");
		try (Stream<Path> paths = Files.list(configRoot)) {
			return paths.filter(path -> path.getFileName().toString().matches("smart-bridge-biz-.+\\.yml"))
					.collect(Collectors.toList());
		}
	}

	private Path locateRepositoryRoot() {
		Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		while (current != null) {
			if (Files.isDirectory(current.resolve("docker/nacos/config/dev"))) {
				return current;
			}
			current = current.getParent();
		}
		throw new IllegalStateException("无法定位仓库根目录");
	}
}
