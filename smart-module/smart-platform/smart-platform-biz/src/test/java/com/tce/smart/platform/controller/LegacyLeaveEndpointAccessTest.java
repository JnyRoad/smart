package com.tce.smart.platform.controller;

import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.data.api.feign.consume.RemoteRsEmpService;
import com.tce.smart.platform.core.dto.LeaveApplicationDTO;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.service.SmtLeaveApplicationService;
import com.tce.smart.platform.service.ILeaveApplicationService;
import com.tce.smart.platform.service.SmtLeaveHandoverService;
import com.tce.smart.platform.service.SmtLbejConfigService;
import com.tce.smart.platform.service.SmtStaffService;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 旧离职入口在未完成调用方盘点前必须默认停用，不能因 server scope 直接写入。 */
public class LegacyLeaveEndpointAccessTest {
	private static final String ACTOR = "A10001";
	private static final String PURPOSE = "leave-legacy-migration";

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void legacyApplicationSaveIsRejectedByDefault() throws Exception {
		Fixture fixture = fixture(false);

		fixture.mockMvc.perform(post("/leave/application/save")
				.contentType("application/json")
				.content(applicationBody()))
				.andExpect(status().isForbidden());
		Mockito.verify(fixture.leaveService, Mockito.never()).saveLeaveApplication(Mockito.any(LeaveApplicationDTO.class));
	}

	@Test
	public void legacyHandoverRoutesAreRejectedByDefault() throws Exception {
		LegacyLeaveEndpointGuard guard = guard(false);
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SmtLeaveHandoverController(
				new OpenApiAuthenticationAdapter(), "legacy-leave-migrator", Mockito.mock(SmtLeaveHandoverService.class), Mockito.mock(SmtLbejConfigService.class),
				Mockito.mock(SmtStaffService.class), Mockito.mock(SmtLeaveApplicationService.class),
				Mockito.mock(ILeaveApplicationService.class), guard))
				.setControllerAdvice(new GlobalExceptionHandlerResolver()).build();

		mockMvc.perform(get("/leave/handover/start/P10001")).andExpect(status().isForbidden());
		mockMvc.perform(get("/leave/handover/get/P10001")).andExpect(status().isForbidden());
		mockMvc.perform(get("/leave/handover/end/P10001")).andExpect(status().isForbidden());
		mockMvc.perform(post("/leave/handover/commit").contentType("application/json")
				.content("{\"processId\":\"P10001\",\"jjr\":\"A10001\"}")).andExpect(status().isForbidden());
		mockMvc.perform(post("/leave/handover/detail").contentType("application/json")
				.content("{\"processId\":\"P10001\"}")).andExpect(status().isForbidden());
	}

	@Test
	public void enabledLegacySaveRejectsGenericClientMissingPurposeAndMissingPark() throws Exception {
		Fixture fixture = fixture(true);
		asClient("generic-server");
		fixture.mockMvc.perform(legacySave("7", PURPOSE)).andExpect(status().isForbidden());

		asClient("legacy-leave-migrator");
		fixture.mockMvc.perform(legacySave("7", null)).andExpect(status().isForbidden());
		fixture.mockMvc.perform(legacySave(null, PURPOSE)).andExpect(status().isForbidden());
		Mockito.verify(fixture.leaveService, Mockito.never()).saveLeaveApplication(Mockito.any(LeaveApplicationDTO.class));
	}

	@Test
	public void enabledLegacySaveAllowsOnlyDedicatedActorAndParkContext() throws Exception {
		Fixture fixture = fixture(true);
		Mockito.when(fixture.leaveService.saveLeaveApplication(Mockito.any(LeaveApplicationDTO.class))).thenReturn(new Result<>(true));
		asClient("legacy-leave-migrator");

		fixture.mockMvc.perform(legacySave("7", PURPOSE)).andExpect(status().isOk());
		Mockito.verify(fixture.leaveService).saveLeaveApplication(Mockito.argThat(request -> ACTOR.equals(request.getBadge())
				&& ACTOR.equals(request.getApplyBadge()) && Integer.valueOf(7).equals(request.getParkId())));
	}

	@Test
	public void enabledLegacyHandoverAllowsDedicatedActorAndMatchingPark() throws Exception {
		SmtLeaveHandoverService handoverService = Mockito.mock(SmtLeaveHandoverService.class);
		SmtLeaveApplicationService applicationService = Mockito.mock(SmtLeaveApplicationService.class);
		SmtLeaveApplication application = new SmtLeaveApplication();
		application.setBadge(ACTOR);
		application.setParkId(7);
		Mockito.when(applicationService.getLeaveApplicationRecord("P10001")).thenReturn(application);
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SmtLeaveHandoverController(
				new OpenApiAuthenticationAdapter(), "legacy-leave-migrator", handoverService, Mockito.mock(SmtLbejConfigService.class),
				Mockito.mock(SmtStaffService.class), applicationService, Mockito.mock(ILeaveApplicationService.class), guard(true)))
				.setControllerAdvice(new GlobalExceptionHandlerResolver()).build();

		asClient("legacy-leave-migrator");
		mockMvc.perform(get("/leave/handover/start/P10001")
				.header("X-Smart-Actor-Badge", ACTOR)
				.header("X-Smart-Actor-Park-Ids", "7")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", PURPOSE))
				.andExpect(status().isOk());
		Mockito.verify(handoverService).startLeaveHandover("P10001");
	}

	private Fixture fixture(boolean enabled) {
		SmtLeaveApplicationService applicationService = Mockito.mock(SmtLeaveApplicationService.class);
		ILeaveApplicationService leaveService = Mockito.mock(ILeaveApplicationService.class);
		SmtLeaveApplicationController controller = new SmtLeaveApplicationController(applicationService, leaveService,
				Mockito.mock(RemoteRsEmpService.class), guard(enabled));
		return new Fixture(leaveService, MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandlerResolver()).build());
	}

	private LegacyLeaveEndpointGuard guard(boolean enabled) {
		LegacyLeaveEndpointGuard guard = new LegacyLeaveEndpointGuard(new OpenApiAuthenticationAdapter());
		ReflectionTestUtils.setField(guard, "enabled", enabled);
		ReflectionTestUtils.setField(guard, "clientId", "legacy-leave-migrator");
		ReflectionTestUtils.setField(guard, "purpose", PURPOSE);
		return guard;
	}

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder legacySave(String parks, String purpose) {
		org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request = post("/leave/application/save")
				.header("X-Smart-Actor-Badge", ACTOR)
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.contentType("application/json")
				.content(applicationBody());
		if (parks != null) {
			request.header("X-Smart-Actor-Park-Ids", parks);
		}
		if (purpose != null) {
			request.header("X-Smart-Internal-Purpose", purpose);
		}
		return request;
	}

	private void asClient(String clientId) {
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), clientId, Collections.emptyList(), true,
				Collections.singleton("server"), Collections.emptySet(), null, Collections.emptySet(), Collections.emptyMap());
		SecurityContextHolder.getContext().setAuthentication(new OAuth2Authentication(request, null));
	}

	private String applicationBody() {
		return "{\"badge\":\"A10001\",\"applyBadge\":\"A10001\",\"parkId\":7,\"leaveType\":1,\"leaveReason\":1,\"leaveTime\":\"2026-07-25\"}";
	}

	private static final class Fixture {
		private final ILeaveApplicationService leaveService;
		private final MockMvc mockMvc;

		private Fixture(ILeaveApplicationService leaveService, MockMvc mockMvc) {
			this.leaveService = leaveService;
			this.mockMvc = mockMvc;
		}
	}
}
