package com.tce.smart.platform.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
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
		Method controllerMethod = InternalParkController.class.getMethod("getBridgeTargets");
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
	public void anonymousAndUserTokensCannotReadBridgeTargetsButServerClientCan() throws Exception {
		SmtParkService service = Mockito.mock(SmtParkService.class);
		SmtPark park = new SmtPark();
		park.setId(10001);
		park.setBridgeUrl("https://bridge.example.invalid:9443");
		Mockito.when(service.getUnStrainedParks()).thenReturn(Collections.singletonList(park));
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new InternalParkController(service))
				.addInterceptors(new OpenApiInterceptor(new OpenApiAuthenticationAdapter()))
				.build();

		mockMvc.perform(get("/internal/park/bridge-targets"))
				.andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("ordinary-user", "N/A", Collections.emptyList()));
		mockMvc.perform(get("/internal/park/bridge-targets"))
				.andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(serverClientAuthentication());
		mockMvc.perform(get("/internal/park/bridge-targets")
					.header(SecurityConstants.FROM, SecurityConstants.FROM_IN))
				.andExpect(status().isOk());
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

		List<InternalParkBridgeTargetRespDTO> response = new InternalParkController(service).getBridgeTargets().getData();
		assertEquals(Integer.valueOf(10001), response.get(0).getId());
		assertEquals("https://bridge.example.invalid:9443", response.get(0).getBridgeUrl());
		Set<String> fieldNames = Arrays.stream(InternalParkBridgeTargetRespDTO.class.getDeclaredFields())
				.filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
				.map(Field::getName).collect(Collectors.toSet());
		assertEquals(new HashSet<>(Arrays.asList("id", "bridgeUrl")), fieldNames);
	}

	private OAuth2Authentication serverClientAuthentication() {
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), "smart-dispatcher",
				Collections.emptyList(), true, Collections.singleton("server"), Collections.emptySet(),
				null, Collections.emptySet(), Collections.emptyMap());
		return new OAuth2Authentication(request, null);
	}
}
