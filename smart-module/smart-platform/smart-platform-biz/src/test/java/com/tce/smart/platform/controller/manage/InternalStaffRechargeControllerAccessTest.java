package com.tce.smart.platform.controller.manage;

import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiInterceptor;
import com.tce.smart.platform.service.manage.SmtStaffRechargeService;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 充值定时任务的内部入口必须拒绝任意非受管服务调用。 */
public class InternalStaffRechargeControllerAccessTest {

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void rechargeEndpointsRequireExactScheduleServerClientAndPurpose() throws Exception {
		SmtStaffRechargeService service = Mockito.mock(SmtStaffRechargeService.class);
		Mockito.when(service.syncNewStaff()).thenReturn(Boolean.TRUE);
		Mockito.when(service.syncSeniorRecharge()).thenReturn(Boolean.TRUE);
		InternalStaffRechargeController controller = new InternalStaffRechargeController(service,
				new OpenApiAuthenticationAdapter());
		ReflectionTestUtils.setField(controller, "scheduleClientId", "smart-schedule");
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.addInterceptors(new OpenApiInterceptor(new OpenApiAuthenticationAdapter()))
				.setControllerAdvice(new GlobalExceptionHandlerResolver())
				.build();

		perform(mockMvc, "/internal/recharge/new", null, null).andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("ordinary-user", "N/A", Collections.emptyList()));
		perform(mockMvc, "/internal/recharge/new", SecurityConstants.FROM_IN, "recharge-task")
				.andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(clientToken("smart-schedule", "other"));
		perform(mockMvc, "/internal/recharge/new", SecurityConstants.FROM_IN, "recharge-task")
				.andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(clientToken("unexpected-client", "server"));
		perform(mockMvc, "/internal/recharge/new", SecurityConstants.FROM_IN, "recharge-task")
				.andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(clientToken("smart-schedule", "server"));
		perform(mockMvc, "/internal/recharge/new", null, "recharge-task").andExpect(status().isForbidden());
		perform(mockMvc, "/internal/recharge/new", "N", "recharge-task").andExpect(status().isForbidden());
		perform(mockMvc, "/internal/recharge/new", SecurityConstants.FROM_IN, null).andExpect(status().isForbidden());
		perform(mockMvc, "/internal/recharge/new", SecurityConstants.FROM_IN, "wrong-purpose")
				.andExpect(status().isForbidden());
		perform(mockMvc, "/internal/recharge/new", SecurityConstants.FROM_IN, "recharge-task")
				.andExpect(status().isOk());
		perform(mockMvc, "/internal/recharge/senior", SecurityConstants.FROM_IN, "recharge-task")
				.andExpect(status().isOk());

		ReflectionTestUtils.setField(controller, "scheduleClientId", "");
		perform(mockMvc, "/internal/recharge/new", SecurityConstants.FROM_IN, "recharge-task")
				.andExpect(status().isForbidden());
	}

	private OAuth2Authentication clientToken(String clientId, String scope) {
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), clientId, Collections.emptyList(), true,
				Collections.singleton(scope), Collections.emptySet(), null, Collections.emptySet(), Collections.emptyMap());
		return new OAuth2Authentication(request, null);
	}

	private org.springframework.test.web.servlet.ResultActions perform(MockMvc mockMvc, String path, String from,
			String purpose) throws Exception {
		org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request = get(path);
		if (from != null) {
			request.header(SecurityConstants.FROM, from);
		}
		if (purpose != null) {
			request.header("X-Smart-Internal-Purpose", purpose);
		}
		return mockMvc.perform(request);
	}
}
