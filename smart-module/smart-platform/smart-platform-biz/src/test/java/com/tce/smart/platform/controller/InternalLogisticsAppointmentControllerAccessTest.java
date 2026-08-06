package com.tce.smart.platform.controller;

import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiInterceptor;
import com.tce.smart.platform.core.dto.LogisticsAppointmentDTO;
import com.tce.smart.platform.service.SmtLogisticsAppointmentService;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 物流定时同步必须走专属内部入口，且不能影响原有管理端保存路由。 */
public class InternalLogisticsAppointmentControllerAccessTest {

	private static final String BODY = "{\"vehiclePlate\":\"粤A12345\",\"startTime\":0,\"planCode\":\"plan-1\"}";

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void logisticsSyncRequiresExactScheduleServerClientAndPurpose() throws Exception {
		SmtLogisticsAppointmentService service = Mockito.mock(SmtLogisticsAppointmentService.class);
		Mockito.when(service.saveLogisticsAppointment(Mockito.any(LogisticsAppointmentDTO.class))).thenReturn(Boolean.TRUE);
		InternalLogisticsAppointmentController controller = new InternalLogisticsAppointmentController(service,
				new OpenApiAuthenticationAdapter());
		ReflectionTestUtils.setField(controller, "scheduleClientId", "smart-schedule");
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.addInterceptors(new OpenApiInterceptor(new OpenApiAuthenticationAdapter()))
				.setControllerAdvice(new GlobalExceptionHandlerResolver())
				.build();

		performInternal(mockMvc, null, null).andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("ordinary-user", "N/A", Collections.emptyList()));
		performInternal(mockMvc, SecurityConstants.FROM_IN, "logistics-sync").andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(clientToken("smart-schedule", "other"));
		performInternal(mockMvc, SecurityConstants.FROM_IN, "logistics-sync").andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(clientToken("unexpected-client", "server"));
		performInternal(mockMvc, SecurityConstants.FROM_IN, "logistics-sync").andExpect(status().isForbidden());

		SecurityContextHolder.getContext().setAuthentication(clientToken("smart-schedule", "server"));
		performInternal(mockMvc, null, "logistics-sync").andExpect(status().isForbidden());
		performInternal(mockMvc, "N", "logistics-sync").andExpect(status().isForbidden());
		performInternal(mockMvc, SecurityConstants.FROM_IN, null).andExpect(status().isForbidden());
		performInternal(mockMvc, SecurityConstants.FROM_IN, "wrong-purpose").andExpect(status().isForbidden());
		performInternal(mockMvc, SecurityConstants.FROM_IN, "logistics-sync").andExpect(status().isOk());

		ReflectionTestUtils.setField(controller, "scheduleClientId", "");
		performInternal(mockMvc, SecurityConstants.FROM_IN, "logistics-sync").andExpect(status().isForbidden());
	}

	@Test
	public void legacyManagementSaveRouteRemainsAvailableAtItsOriginalPath() throws Exception {
		SmtLogisticsAppointmentService service = Mockito.mock(SmtLogisticsAppointmentService.class);
		Mockito.when(service.saveLogisticsAppointment(Mockito.any(LogisticsAppointmentDTO.class))).thenReturn(Boolean.TRUE);
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SmtLogisticsAppointmentController(service)).build();

		mockMvc.perform(post("/logistics/appointment/save")
				.contentType(MediaType.APPLICATION_JSON)
				.content(BODY))
				.andExpect(status().isOk());
	}

	private OAuth2Authentication clientToken(String clientId, String scope) {
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), clientId, Collections.emptyList(), true,
				Collections.singleton(scope), Collections.emptySet(), null, Collections.emptySet(), Collections.emptyMap());
		return new OAuth2Authentication(request, null);
	}

	private org.springframework.test.web.servlet.ResultActions performInternal(MockMvc mockMvc, String from,
			String purpose) throws Exception {
		org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request = post("/internal/logistics/appointment/save")
				.contentType(MediaType.APPLICATION_JSON)
				.content(BODY);
		if (from != null) {
			request.header(SecurityConstants.FROM, from);
		}
		if (purpose != null) {
			request.header("X-Smart-Internal-Purpose", purpose);
		}
		return mockMvc.perform(request);
	}
}
