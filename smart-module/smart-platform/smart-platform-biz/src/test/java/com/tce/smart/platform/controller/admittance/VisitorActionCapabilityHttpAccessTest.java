package com.tce.smart.platform.controller.admittance;

import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiInterceptor;
import com.tce.smart.platform.service.admittance.VisitorFaceCropCapabilityService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 真实 HTTP 入口验证：公开签发只能接受允许动作，内部消费端点永不随 Nacos 匿名放行。
 */
public class VisitorActionCapabilityHttpAccessTest {

	private VisitorFaceCropCapabilityService capabilityService;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		capabilityService = Mockito.mock(VisitorFaceCropCapabilityService.class);
		VisitorActionCapabilityController controller = new VisitorActionCapabilityController(capabilityService,
				new OpenApiAuthenticationAdapter());
		ReflectionTestUtils.setField(controller, "appServiceClientId", "smart-app");
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.addInterceptors(new OpenApiInterceptor(new OpenApiAuthenticationAdapter()))
				.setControllerAdvice(new GlobalExceptionHandlerResolver())
				.build();
	}

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void publicCapabilityEndpointRejectsCropAndFaceUploadActions() throws Exception {
		mockMvc.perform(post("/admittance/visitor-action/capability")
				.contentType("application/json")
				.content("{\"draftId\":\"draft-1\",\"action\":\"FACE_UPLOAD\",\"payloadHash\":\""
						+ repeat('a', 64) + "\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(1));
		Mockito.verifyZeroInteractions(capabilityService);
	}

	@Test
	public void publicCapabilityEndpointAllowsOnlyPayloadBoundApplyPrecheck() throws Exception {
		mockMvc.perform(post("/admittance/visitor-action/capability")
				.header("X-Visitor-Draft-Token", "draft-token")
				.contentType("application/json")
				.content("{\"draftId\":\"draft-1\",\"action\":\"APPLY_PRECHECK\",\"payloadHash\":\""
						+ repeat('a', 64) + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(0));
		Mockito.verify(capabilityService).issueActionCapability("draft-token", "draft-1",
				com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction.APPLY_PRECHECK, repeat('a', 64));
	}

	@Test
	public void internalConsumeEndpointRejectsUnauthenticatedBrowserBeforeCapabilityLookup() throws Exception {
		mockMvc.perform(post("/admittance/visitor-action/internal/consume")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.contentType("application/json")
				.content("{\"capability\":\"ticket\",\"draftId\":\"draft-1\",\"action\":\"BLACKLIST_CHECK\"}"))
				.andExpect(status().isForbidden());
		Mockito.verifyZeroInteractions(capabilityService);
	}

	@Test
	public void configuredAppServiceCanConsumeCropDerivedFaceUploadCapability() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(serverClientAuthentication("smart-app"));
		mockMvc.perform(post("/admittance/visitor-action/internal/consume")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.contentType("application/json")
				.content("{\"capability\":\"face-ticket\",\"draftId\":\"draft-1\",\"action\":\"FACE_UPLOAD\",\"payloadHash\":\""
						+ repeat('a', 64) + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(0));
		Mockito.verify(capabilityService).consumeActionCapability("face-ticket", "draft-1",
				com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction.FACE_UPLOAD, repeat('a', 64));
	}

	private OAuth2Authentication serverClientAuthentication(String clientId) {
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), clientId, Collections.emptyList(), true,
				Collections.singleton("server"), Collections.emptySet(), null, Collections.emptySet(), Collections.emptyMap());
		return new OAuth2Authentication(request, null);
	}

	private String repeat(char value, int length) {
		StringBuilder result = new StringBuilder(length);
		for (int index = 0; index < length; index++) {
			result.append(value);
		}
		return result.toString();
	}
}
