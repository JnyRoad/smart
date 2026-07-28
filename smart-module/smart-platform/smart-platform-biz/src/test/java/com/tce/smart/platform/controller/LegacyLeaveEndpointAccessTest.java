package com.tce.smart.platform.controller;

import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.data.api.feign.consume.RemoteRsEmpService;
import com.tce.smart.platform.core.dto.LeaveApplicationDTO;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.entity.SmtLeaveHandover;
import com.tce.smart.platform.core.service.SmtLeaveApplicationService;
import com.tce.smart.platform.core.vo.LeaveRecordVO;
import com.tce.smart.platform.service.ILeaveApplicationService;
import com.tce.smart.platform.service.SmtLeaveHandoverService;
import com.tce.smart.platform.service.SmtLbejConfigService;
import com.tce.smart.platform.service.SmtStaffService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
	public void enabledLegacyHandoverRoutesAllowDedicatedActorAndMatchingPark() throws Exception {
		SmtLeaveHandoverService handoverService = Mockito.mock(SmtLeaveHandoverService.class);
		SmtLeaveApplicationService applicationService = Mockito.mock(SmtLeaveApplicationService.class);
		SmtLeaveApplication application = new SmtLeaveApplication();
		application.setBadge(ACTOR);
		application.setParkId(7);
		Mockito.when(applicationService.getLeaveApplicationRecord("P10001")).thenReturn(application);
		Mockito.when(handoverService.getLeaveHandover("P10001", ACTOR))
				.thenReturn(Collections.singletonList(new SmtLeaveHandover()));
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SmtLeaveHandoverController(
				new OpenApiAuthenticationAdapter(), "legacy-leave-migrator", handoverService, Mockito.mock(SmtLbejConfigService.class),
				Mockito.mock(SmtStaffService.class), applicationService, Mockito.mock(ILeaveApplicationService.class), guard(true)))
				.setControllerAdvice(new GlobalExceptionHandlerResolver()).build();

		asClient("legacy-leave-migrator");
		mockMvc.perform(legacyHandoverGet("/leave/handover/start/P10001"))
				.andExpect(status().isOk());
		mockMvc.perform(legacyHandoverGet("/leave/handover/get/P10001"))
				.andExpect(status().isOk());
		mockMvc.perform(legacyHandoverGet("/leave/handover/end/P10001"))
				.andExpect(status().isOk());
		mockMvc.perform(legacyHandoverPost("/leave/handover/commit", "{\"processId\":\"P10001\",\"jjr\":\"SPOOFED\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(legacyHandoverPost("/leave/handover/detail", "{\"processId\":\"P10001\"}"))
				.andExpect(status().isOk());
		Mockito.verify(handoverService).startLeaveHandover("P10001");
		Mockito.verify(handoverService).getLeaveHandoverByProcessId("P10001");
		Mockito.verify(handoverService).closeLeaveHandover("P10001");
		Mockito.verify(handoverService).endLeaveHandover(Mockito.argThat(request -> ACTOR.equals(request.getJjr())));
		Mockito.verify(handoverService).getLeaveHandover("P10001");
	}

	@Test
	public void enabledLegacyHandoverRejectsActorOutsideApplication() throws Exception {
		SmtLeaveHandoverService handoverService = Mockito.mock(SmtLeaveHandoverService.class);
		SmtLeaveApplicationService applicationService = Mockito.mock(SmtLeaveApplicationService.class);
		SmtLeaveApplication application = new SmtLeaveApplication();
		application.setBadge("A20002");
		application.setParkId(7);
		Mockito.when(applicationService.getLeaveApplicationRecord("P10001")).thenReturn(application);
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SmtLeaveHandoverController(
				new OpenApiAuthenticationAdapter(), "legacy-leave-migrator", handoverService, Mockito.mock(SmtLbejConfigService.class),
				Mockito.mock(SmtStaffService.class), applicationService, Mockito.mock(ILeaveApplicationService.class), guard(true)))
				.setControllerAdvice(new GlobalExceptionHandlerResolver()).build();

		asClient("legacy-leave-migrator");
		mockMvc.perform(legacyHandoverGet("/leave/handover/get/P10001")).andExpect(status().isForbidden());
		Mockito.verify(handoverService, Mockito.never()).getLeaveHandoverByProcessId("P10001");
	}

	@Test
	public void enabledLegacyRecordPageRejectsGenericClientAndMissingActor() throws Exception {
		Fixture fixture = fixture(true);
		asClient("generic-server");
		fixture.mockMvc.perform(legacyRecordPage(ACTOR, ACTOR, "7")).andExpect(status().isForbidden());

		asClient("legacy-leave-migrator");
		fixture.mockMvc.perform(legacyRecordPage(ACTOR, null, "7")).andExpect(status().isForbidden());
		Mockito.verify(fixture.applicationService, Mockito.never()).getProcessRecord(
				Mockito.any(Page.class), Mockito.anyString(), Mockito.anyInt(), Mockito.anySet());
	}

	@Test
	public void enabledLegacyRecordPageRejectsForeignRequestedBadge() throws Exception {
		Fixture fixture = fixture(true);
		asClient("legacy-leave-migrator");

		fixture.mockMvc.perform(legacyRecordPage("A20002", ACTOR, "7")).andExpect(status().isForbidden());
		Mockito.verify(fixture.applicationService, Mockito.never()).getProcessRecord(
				Mockito.any(Page.class), Mockito.anyString(), Mockito.anyInt(), Mockito.anySet());
	}

	@Test
	public void enabledLegacyRecordDetailRejectsActorOutsideRecordPark() throws Exception {
		Fixture fixture = fixture(true);
		SmtLeaveApplication application = new SmtLeaveApplication();
		application.setBadge(ACTOR);
		application.setParkId(8);
		Mockito.when(fixture.applicationService.getLeaveApplicationRecord("P10001")).thenReturn(application);
		asClient("legacy-leave-migrator");

		fixture.mockMvc.perform(legacyRecordDetail("P10001", ACTOR, "7")).andExpect(status().isForbidden());
		Mockito.verify(fixture.applicationService, Mockito.never()).getLeaveApplication("P10001");
	}

	@Test
	public void enabledLegacyRecordPageUsesDedicatedActorAndParkScope() throws Exception {
		Fixture fixture = fixture(true);
		Mockito.when(fixture.applicationService.getProcessRecord(Mockito.any(Page.class), Mockito.eq(ACTOR),
				Mockito.eq(1), Mockito.anySet())).thenReturn(new Page<LeaveRecordVO>());
		asClient("legacy-leave-migrator");

		fixture.mockMvc.perform(legacyRecordPage(ACTOR, ACTOR, "7")).andExpect(status().isOk());
		Mockito.verify(fixture.applicationService).getProcessRecord(Mockito.any(Page.class), Mockito.eq(ACTOR),
				Mockito.eq(1), Mockito.argThat(parks -> parks.size() == 1 && parks.contains(7)));
	}

	@Test
	public void enabledLegacyRecordDetailAllowsDedicatedActorAndMatchingPark() throws Exception {
		Fixture fixture = fixture(true);
		SmtLeaveApplication application = new SmtLeaveApplication();
		application.setBadge(ACTOR);
		application.setParkId(7);
		Mockito.when(fixture.applicationService.getLeaveApplicationRecord("P10001")).thenReturn(application);
		Mockito.when(fixture.applicationService.getLeaveApplication("P10001")).thenReturn(Collections.emptyList());
		asClient("legacy-leave-migrator");

		fixture.mockMvc.perform(legacyRecordDetail("P10001", ACTOR, "7")).andExpect(status().isOk());
		Mockito.verify(fixture.applicationService).getLeaveApplication("P10001");
	}

	private Fixture fixture(boolean enabled) {
		SmtLeaveApplicationService applicationService = Mockito.mock(SmtLeaveApplicationService.class);
		ILeaveApplicationService leaveService = Mockito.mock(ILeaveApplicationService.class);
		SmtLeaveApplicationController controller = new SmtLeaveApplicationController(applicationService, leaveService,
				Mockito.mock(RemoteRsEmpService.class), guard(enabled));
		return new Fixture(applicationService, leaveService, MockMvcBuilders.standaloneSetup(controller)
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

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder legacyHandoverGet(String path) {
		return get(path)
				.header("X-Smart-Actor-Badge", ACTOR)
				.header("X-Smart-Actor-Park-Ids", "7")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", PURPOSE);
	}

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder legacyHandoverPost(String path, String body) {
		return post(path)
				.header("X-Smart-Actor-Badge", ACTOR)
				.header("X-Smart-Actor-Park-Ids", "7")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", PURPOSE)
				.contentType("application/json")
				.content(body);
	}

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder legacyRecordPage(String badge, String actor, String parks) {
		org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request = get("/leave/application/record/page")
				.param("badge", badge)
				.param("leaveStatus", "1")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", PURPOSE);
		if (actor != null) {
			request.header("X-Smart-Actor-Badge", actor);
		}
		if (parks != null) {
			request.header("X-Smart-Actor-Park-Ids", parks);
		}
		return request;
	}

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder legacyRecordDetail(String processId, String actor, String parks) {
		org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request = get("/leave/application/record/detail/{processId}", processId)
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", PURPOSE);
		if (actor != null) {
			request.header("X-Smart-Actor-Badge", actor);
		}
		if (parks != null) {
			request.header("X-Smart-Actor-Park-Ids", parks);
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
		private final SmtLeaveApplicationService applicationService;
		private final ILeaveApplicationService leaveService;
		private final MockMvc mockMvc;

		private Fixture(SmtLeaveApplicationService applicationService, ILeaveApplicationService leaveService, MockMvc mockMvc) {
			this.applicationService = applicationService;
			this.leaveService = leaveService;
			this.mockMvc = mockMvc;
		}
	}
}
