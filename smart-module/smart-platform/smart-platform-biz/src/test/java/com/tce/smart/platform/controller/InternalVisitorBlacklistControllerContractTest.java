package com.tce.smart.platform.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiInterceptor;
import com.tce.smart.platform.api.dto.SmtVisitorDTO;
import com.tce.smart.platform.api.feign.RemoteVisitorService;
import com.tce.smart.platform.service.SmtVisitorService;
import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 黑名单结果仅允许 App 服务端经专用内部路由取得，不得复用面向外部的访客控制器。
 */
public class InternalVisitorBlacklistControllerContractTest {

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void platformInternalBlacklistRoutesRequireServerTokenAndPurpose() throws Exception {
		Class<?> controller = Class.forName("com.tce.smart.platform.controller.InternalVisitorBlacklistController");
		assertRoute(controller, "checkVisitor", "/visitor");
		assertRoute(controller, "checkVehicle", "/vehicle");
	}

	@Test
	public void legacyVisitorControllerDoesNotExposeBlacklistQueryRoutes() {
		for (Method method : SmtVisitorController.class.getDeclaredMethods()) {
			PostMapping mapping = method.getAnnotation(PostMapping.class);
			if (mapping == null) {
				continue;
			}
			for (String route : mapping.value()) {
				assertFalse("黑名单查询只能通过专用内部接口", "/checkBlackVisitor".equals(route));
				assertFalse("黑名单查询只能通过专用内部接口", "/checkBlackVehicle".equals(route));
			}
		}
	}

	private void assertRoute(Class<?> controller, String methodName, String route) throws Exception {
		Method controllerMethod = controller.getMethod(methodName, SmtVisitorDTO.class, String.class, String.class);
		assertNotNull(controllerMethod.getAnnotation(Inner.class));
		assertEquals("server", controllerMethod.getAnnotation(OpenApi.class).value());
		assertEquals(route, controllerMethod.getAnnotation(PostMapping.class).value()[0]);
		assertEquals(SecurityConstants.FROM,
				controllerMethod.getParameters()[1].getAnnotation(RequestHeader.class).value());
		assertEquals("X-Smart-Internal-Purpose",
				controllerMethod.getParameters()[2].getAnnotation(RequestHeader.class).value());

		Method feignMethod = RemoteVisitorService.class.getMethod(
				"/visitor".equals(route) ? "checkVisitorBlacklist" : "checkVehicleBlacklist",
				SmtVisitorDTO.class, String.class, String.class, String.class);
		assertEquals("/internal/visitor-blacklist" + route,
				feignMethod.getAnnotation(PostMapping.class).value()[0]);
	}

	@Test
	public void onlyConfiguredAppClientWithExactPurposeCanCheckBlacklist() throws Exception {
		SmtVisitorService service = Mockito.mock(SmtVisitorService.class);
		Mockito.when(service.checkBlackVisitor(Mockito.any())).thenReturn(Boolean.TRUE);
		InternalVisitorBlacklistController controller = new InternalVisitorBlacklistController(service,
				new OpenApiAuthenticationAdapter());
		ReflectionTestUtils.setField(controller, "appServiceClientId", "smart-app");
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.addInterceptors(new OpenApiInterceptor(new OpenApiAuthenticationAdapter()))
				.setControllerAdvice(new GlobalExceptionHandlerResolver())
				.build();

		mockMvc.perform(post("/internal/visitor-blacklist/visitor")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", "visitor-blacklist")
				.contentType("application/json").content("{\"certNo\":\"110101199001010011\",\"parkId\":1}"))
				.andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(serverClientAuthentication("other-client"));
		mockMvc.perform(post("/internal/visitor-blacklist/visitor")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", "visitor-blacklist")
				.contentType("application/json").content("{\"certNo\":\"110101199001010011\",\"parkId\":1}"))
				.andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(serverClientAuthentication("smart-app"));
		mockMvc.perform(post("/internal/visitor-blacklist/visitor")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", "other-purpose")
				.contentType("application/json").content("{\"certNo\":\"110101199001010011\",\"parkId\":1}"))
				.andExpect(status().isForbidden());
		mockMvc.perform(post("/internal/visitor-blacklist/visitor")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", "visitor-blacklist")
				.contentType("application/json").content("{\"certNo\":\"110101199001010011\",\"parkId\":1}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));

		ReflectionTestUtils.setField(controller, "appServiceClientId", "");
		mockMvc.perform(post("/internal/visitor-blacklist/visitor")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", "visitor-blacklist")
				.contentType("application/json").content("{\"certNo\":\"110101199001010011\",\"parkId\":1}"))
				.andExpect(status().isForbidden());
	}

	private OAuth2Authentication serverClientAuthentication(String clientId) {
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), clientId, Collections.emptyList(), true,
				Collections.singleton("server"), Collections.emptySet(), null, Collections.emptySet(), Collections.emptyMap());
		return new OAuth2Authentication(request, null);
	}
}
