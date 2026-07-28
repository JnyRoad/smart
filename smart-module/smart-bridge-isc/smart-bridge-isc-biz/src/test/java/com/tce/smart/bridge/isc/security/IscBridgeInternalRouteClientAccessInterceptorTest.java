package com.tce.smart.bridge.isc.security;

import com.tce.smart.bridge.isc.controller.BridgeISCController;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** ISC Bridge 必须复用统一精确 client_id 守卫，不能仅依赖 server scope。 */
public class IscBridgeInternalRouteClientAccessInterceptorTest {

	private static final String DISPATCHER_CLIENT = "dispatcher-client";
	private static final String OTHER_SERVER_CLIENT = "another-server-client";

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void anotherServerClientPassesOpenApiButIsRejectedByTheSharedGuard() throws Exception {
		InternalServerRouteClientAccessInterceptor interceptor = interceptor(DISPATCHER_CLIENT);
		HandlerMethod handler = iscHandler("handle", String.class);
		SecurityContextHolder.getContext().setAuthentication(serverAuthentication(OTHER_SERVER_CLIENT));

		assertTrue("通用 OpenApi 守卫只校验 server scope，另一服务 client 会通过",
				new OpenApiInterceptor(new OpenApiAuthenticationAdapter()).preHandle(
						request(SecurityConstants.FROM_IN), new MockHttpServletResponse(), handler));
		assertDenied(interceptor, handler, SecurityConstants.FROM_IN);
	}

	@Test
	public void configuredDispatcherClientWithInternalSourceIsAllowed() throws Exception {
		InternalServerRouteClientAccessInterceptor interceptor = interceptor(DISPATCHER_CLIENT);
		HandlerMethod handler = iscHandler("dispatch", com.tce.smart.bridge.isc.api.dto.req.BridgeDTO.class);
		SecurityContextHolder.getContext().setAuthentication(serverAuthentication(DISPATCHER_CLIENT));

		assertTrue(interceptor.preHandle(request(SecurityConstants.FROM_IN), new MockHttpServletResponse(), handler));
	}

	@Test
	public void blankConfigurationAndWrongSourceFailClosed() throws Exception {
		HandlerMethod handler = iscHandler("getImage", com.tce.smart.bridge.isc.api.dto.req.ImageDTO.class);
		SecurityContextHolder.getContext().setAuthentication(serverAuthentication(DISPATCHER_CLIENT));

		assertDenied(interceptor(""), handler, SecurityConstants.FROM_IN);
		assertDenied(interceptor(DISPATCHER_CLIENT), handler, "N");
		assertDenied(interceptor(DISPATCHER_CLIENT), handler, null);
	}

	@Test
	public void iscNacosTemplatesDefaultToAnEmptyAllowlist() throws IOException {
		for (Path template : iscNacosTemplates()) {
			String source = new String(Files.readAllBytes(template), StandardCharsets.UTF_8);
			assertTrue(template + " 必须显式注入 ISC Bridge 允许调用方",
					source.contains("allowed-client-ids: \"${SMART_BRIDGE_ISC_ALLOWED_CLIENT_IDS:}\""));
		}
	}

	private InternalServerRouteClientAccessInterceptor interceptor(String allowedClientIds) {
		InternalServerRouteClientAccessProperties properties = new InternalServerRouteClientAccessProperties();
		properties.setAllowedClientIds(allowedClientIds);
		return new InternalServerRouteClientAccessInterceptor(new OpenApiAuthenticationAdapter(), properties);
	}

	private HandlerMethod iscHandler(String methodName, Class<?> parameterType) throws Exception {
		Method method = BridgeISCController.class.getMethod(methodName, parameterType);
		return new HandlerMethod(new BridgeISCController(null, null, null), method);
	}

	private void assertDenied(InternalServerRouteClientAccessInterceptor interceptor, HandlerMethod handler,
			String from) throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		assertFalse(interceptor.preHandle(request(from), response, handler));
		assertTrue("统一精确 client_id 守卫拒绝时必须返回 403", response.getStatus() == 403);
	}

	private MockHttpServletRequest request(String from) {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/bridge/handle");
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

	private List<Path> iscNacosTemplates() {
		Path configRoot = locateRepositoryRoot().resolve("docker/nacos/config/dev");
		return Arrays.asList(configRoot.resolve("smart-bridge-isc.yml"),
				configRoot.resolve("smart-bridge-isc-biz-5000021.yml"));
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
