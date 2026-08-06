package com.tce.smart.platform.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiInterceptor;
import com.tce.smart.platform.api.dto.resp.InternalParkBridgeTargetRespDTO;
import com.tce.smart.platform.api.feign.RemoteParkInternalService;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.service.SmtParkService;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 动态 Bridge 目标只能由持有 server scope 的服务读取，不能复用面向客户端的园区目录。
 */
public class InternalParkControllerAccessTest {

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void internalBridgeTargetRouteHasServiceTokenContract() throws Exception {
		Method controllerMethod = InternalParkController.class.getMethod("getBridgeTargets", String.class);
		assertNotNull(controllerMethod.getAnnotation(Inner.class));
		assertEquals("server", controllerMethod.getAnnotation(OpenApi.class).value());
		assertEquals("/bridge-targets", controllerMethod.getAnnotation(GetMapping.class).value()[0]);

		Method feignMethod = RemoteParkInternalService.class.getMethod("getBridgeTargets", String.class, String.class);
		assertEquals("/internal/park/bridge-targets", feignMethod.getAnnotation(GetMapping.class).value()[0]);
		assertEquals(SecurityConstants.FROM,
				feignMethod.getParameters()[0].getAnnotation(RequestHeader.class).value());
		assertEquals(SecurityConstants.INTERNAL_SERVICE_AUTH,
				feignMethod.getParameters()[1].getAnnotation(RequestHeader.class).value());
	}

	@Test
	public void onlyConfiguredDispatcherServerClientCanReadBridgeTargets() throws Exception {
		SmtParkService service = Mockito.mock(SmtParkService.class);
		SmtPark park = new SmtPark();
		park.setId(10001);
		park.setBridgeUrl("https://bridge.example.invalid:9443");
		Mockito.when(service.getUnStrainedParks()).thenReturn(Collections.singletonList(park));
		OpenApiAuthenticationAdapter adapter = new OpenApiAuthenticationAdapter();
		InternalParkController controller = controller(service, adapter, "smart-dispatcher");
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.addInterceptors(new OpenApiInterceptor(new OpenApiAuthenticationAdapter()))
				.setControllerAdvice(new GlobalExceptionHandlerResolver())
				.build();

		mockMvc.perform(get("/internal/park/bridge-targets"))
				.andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("ordinary-user", "N/A", Collections.emptyList()));
		mockMvc.perform(get("/internal/park/bridge-targets").header(SecurityConstants.FROM, SecurityConstants.FROM_IN))
				.andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(serverClientAuthentication("smart-dispatcher", "other"));
		mockMvc.perform(get("/internal/park/bridge-targets").header(SecurityConstants.FROM, SecurityConstants.FROM_IN))
				.andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(serverClientAuthentication("other-server-client", "server"));
		mockMvc.perform(get("/internal/park/bridge-targets").header(SecurityConstants.FROM, SecurityConstants.FROM_IN))
				.andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(serverClientAuthentication("smart-dispatcher", "server"));
		mockMvc.perform(get("/internal/park/bridge-targets"))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/internal/park/bridge-targets").header(SecurityConstants.FROM, "N"))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/internal/park/bridge-targets")
					.header(SecurityConstants.FROM, SecurityConstants.FROM_IN))
				.andExpect(status().isOk());

		ReflectionTestUtils.setField(controller, "dispatcherServiceClientId", "");
		mockMvc.perform(get("/internal/park/bridge-targets")
					.header(SecurityConstants.FROM, SecurityConstants.FROM_IN))
				.andExpect(status().isForbidden());
	}

	@Test
	public void responseContainsOnlyBridgeTargetFields() {
		SmtParkService service = Mockito.mock(SmtParkService.class);
		SmtPark park = new SmtPark();
		park.setId(10001);
		park.setBridgeUrl("https://bridge.example.invalid:9443");
		park.setParkName("不应返回");
		park.setParkPhone("13800138000");
		Mockito.when(service.getUnStrainedParks()).thenReturn(Collections.singletonList(park));

		OpenApiAuthenticationAdapter adapter = new OpenApiAuthenticationAdapter();
		SecurityContextHolder.getContext().setAuthentication(serverClientAuthentication("smart-dispatcher", "server"));
		List<InternalParkBridgeTargetRespDTO> response = controller(service, adapter, "smart-dispatcher")
				.getBridgeTargets(SecurityConstants.FROM_IN).getData();
		assertEquals(Integer.valueOf(10001), response.get(0).getId());
		assertEquals("https://bridge.example.invalid:9443", response.get(0).getBridgeUrl());
		Set<String> fieldNames = Arrays.stream(InternalParkBridgeTargetRespDTO.class.getDeclaredFields())
				.filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
				.map(Field::getName).collect(Collectors.toSet());
		assertEquals(new HashSet<>(Arrays.asList("id", "bridgeUrl")), fieldNames);
	}

	@Test
	public void onlyConfiguredServerClientsWithExactPurposeCanReadAllParks() throws Exception {
		SmtParkService service = Mockito.mock(SmtParkService.class);
		Mockito.when(service.getUnStrainedParks()).thenReturn(Collections.emptyList());
		InternalParkController controller = controller(service, new OpenApiAuthenticationAdapter(), "smart-dispatcher");
		ReflectionTestUtils.setField(controller, "parkListClientIds", "smart-app,smart-schedule");
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.addInterceptors(new OpenApiInterceptor(new OpenApiAuthenticationAdapter()))
				.setControllerAdvice(new GlobalExceptionHandlerResolver())
				.build();

		mockMvc.perform(get("/internal/park/all")).andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("ordinary-user", "N/A", Collections.emptyList()));
		performAllParks(mockMvc, SecurityConstants.FROM_IN, "park-list").andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(serverClientAuthentication("smart-app", "other"));
		performAllParks(mockMvc, SecurityConstants.FROM_IN, "park-list").andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(serverClientAuthentication("unexpected-client", "server"));
		performAllParks(mockMvc, SecurityConstants.FROM_IN, "park-list").andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(serverClientAuthentication("smart-app", "server"));
		performAllParks(mockMvc, null, "park-list").andExpect(status().isForbidden());
		performAllParks(mockMvc, "N", "park-list").andExpect(status().isForbidden());
		performAllParks(mockMvc, SecurityConstants.FROM_IN, null).andExpect(status().isForbidden());
		performAllParks(mockMvc, SecurityConstants.FROM_IN, "wrong-purpose").andExpect(status().isForbidden());
		performAllParks(mockMvc, SecurityConstants.FROM_IN, "park-list").andExpect(status().isOk());

		SecurityContextHolder.getContext().setAuthentication(serverClientAuthentication("smart-schedule", "server"));
		performAllParks(mockMvc, SecurityConstants.FROM_IN, "park-list").andExpect(status().isOk());

		ReflectionTestUtils.setField(controller, "parkListClientIds", "");
		performAllParks(mockMvc, SecurityConstants.FROM_IN, "park-list").andExpect(status().isForbidden());
	}

	private InternalParkController controller(SmtParkService service, OpenApiAuthenticationAdapter adapter, String clientId) {
		InternalParkController controller = new InternalParkController(service, adapter);
		ReflectionTestUtils.setField(controller, "dispatcherServiceClientId", clientId);
		return controller;
	}

	private OAuth2Authentication serverClientAuthentication(String clientId, String scope) {
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), clientId,
				Collections.emptyList(), true, Collections.singleton(scope), Collections.emptySet(),
				null, Collections.emptySet(), Collections.emptyMap());
		return new OAuth2Authentication(request, null);
	}

	private org.springframework.test.web.servlet.ResultActions performAllParks(MockMvc mockMvc, String from,
			String purpose) throws Exception {
		org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request = get("/internal/park/all");
		if (from != null) {
			request.header(SecurityConstants.FROM, from);
		}
		if (purpose != null) {
			request.header("X-Smart-Internal-Purpose", purpose);
		}
		return mockMvc.perform(request);
	}
}
