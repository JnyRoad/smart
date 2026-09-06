package com.tce.smart.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceConflictException;
import com.tce.smart.platform.dto.authgovernance.AuthOperationManualVerificationRequest;
import com.tce.smart.platform.dto.authgovernance.AuthOperationRetryRequest;
import com.tce.smart.platform.service.impl.AuthOperationManagementActionService;
import org.junit.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Controller 契约覆盖精确权限表达式与严格 JSON，避免只靠前端隐藏字段。 */
public class AuthOperationGovernanceControllerTest {

	@Test
	public void everyGovernanceRouteHasItsExplicitPermission() throws Exception {
		assertPermission("getParkReviews", "platform_auth_operation_review_view", GetMapping.class);
		assertPermission("getGlobalReviews", "platform_auth_operation_global_review_view", GetMapping.class);
		assertPermission("retry", "platform_auth_operation_retry", PostMapping.class);
		assertPermission("manualVerification", "platform_auth_operation_manual_verify", PostMapping.class);
		assertPermission("getActions", "platform_auth_operation_manual_verify", GetMapping.class);
		assertPermission("getAction", "platform_auth_operation_manual_verify", GetMapping.class);
	}

	@Test
	public void retryJsonRejectsForgedActorAndAllowedParks() {
		String json = "{\"idempotencyKey\":\"x\",\"reasonText\":\"r\",\"actorUserId\":1,\"allowedParkIds\":[17],\"targets\":[]}";
		assertThatThrownBy(() -> new ObjectMapper().readValue(json, AuthOperationRetryRequest.class))
				.hasRootCauseInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void manualJsonRejectsTrustedOrExternalIdentifiers() {
		String json = "{\"idempotencyKey\":\"x\",\"expectedOperationVersion\":\"1\",\"expectedAttemptId\":\"2\","
				+ "\"expectedState\":\"VERIFYING\",\"observedConclusion\":\"PERMISSION_ABSENT\",\"reasonText\":\"r\","
				+ "\"trusted\":true,\"externalCommandId\":\"fake\"}";
		assertThatThrownBy(() -> new ObjectMapper().readValue(json, AuthOperationManualVerificationRequest.class))
				.hasRootCauseInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void payloadConflictIsHttp409WithBothRealAdviceOrders() throws Exception {
		AuthOperationManagementActionService service = mock(AuthOperationManagementActionService.class);
		when(service.retry(any())).thenThrow(new AuthOperationGovernanceConflictException("same key, changed payload"));
		Object global = new GlobalExceptionHandlerResolver();
		Object governance = new AuthOperationGovernanceControllerAdvice();
		for (boolean globalFirst : new boolean[]{true, false}) {
			MockMvc mvc = MockMvcBuilders.standaloneSetup(new AuthOperationGovernanceController(service))
					.setControllerAdvice(globalFirst ? new Object[]{global, governance} : new Object[]{governance, global})
					.build();
			assertThat(mvc.perform(post("/device/authority/operation/target/retry")
					.contentType("application/json")
					.content("{\"idempotencyKey\":\"key\",\"reasonText\":\"reason\",\"targets\":[]}"))
					.andReturn().getResponse().getStatus()).isEqualTo(409);
		}
	}

	private void assertPermission(String methodName, String permission, Class<?> routeAnnotation) {
		Method found = null;
		for (Method method : AuthOperationGovernanceController.class.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) found = method;
		}
		assertThat(found).isNotNull();
		PreAuthorize preAuthorize = found.getAnnotation(PreAuthorize.class);
		assertThat(preAuthorize).isNotNull();
		assertThat(preAuthorize.value()).contains(permission);
		assertThat(found.getAnnotation((Class) routeAnnotation)).isNotNull();
	}
}
