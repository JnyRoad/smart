package com.tce.smart.platform.controller;

import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.platform.core.dto.LeaveHandoverDTO;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.entity.SmtLeaveHandover;
import com.tce.smart.platform.core.service.SmtLeaveApplicationService;
import com.tce.smart.platform.core.vo.LeaveRecordVO;
import com.tce.smart.platform.service.ILeaveApplicationService;
import com.tce.smart.platform.service.SmtLeaveHandoverService;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 离职和交接的 processId 不是授权凭据，必须受到 actor 与园区二次校验。 */
public class InternalAppLeaveControllerAccessTest {
	private static final String ACTOR = "A10001";
	private static final String PROCESS = "P10001";
	private static final String PURPOSE = "app-leave-self";

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void applicationRejectsProcessFromAnotherPark() throws Exception {
		Fixture fixture = fixture(7);
		asAppClient("smart-app");

		fixture.mockMvc.perform(authorisedGet("/internal/app-leave/application/" + PROCESS, "8"))
				.andExpect(status().isForbidden());
	}

	@Test
	public void applicationRejectsWrongClientSourcePurposeAndEmptyClientConfiguration() throws Exception {
		Fixture fixture = fixture(7);
		asAppClient("other-client");
		fixture.mockMvc.perform(authorisedGet("/internal/app-leave/application/" + PROCESS, "7"))
				.andExpect(status().isForbidden());

		asAppClient("smart-app");
		fixture.mockMvc.perform(get("/internal/app-leave/application/{processId}", PROCESS)
				.header("X-Smart-Actor-Badge", ACTOR)
				.header("X-Smart-Actor-Park-Ids", "7")
				.header(SecurityConstants.FROM, "external")
				.header("X-Smart-Internal-Purpose", PURPOSE))
				.andExpect(status().isForbidden());
		fixture.mockMvc.perform(get("/internal/app-leave/application/{processId}", PROCESS)
				.header("X-Smart-Actor-Badge", ACTOR)
				.header("X-Smart-Actor-Park-Ids", "7")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", "other-purpose"))
				.andExpect(status().isForbidden());

		ReflectionTestUtils.setField(fixture.controller, "appServiceClientId", "");
		fixture.mockMvc.perform(authorisedGet("/internal/app-leave/application/" + PROCESS, "7"))
				.andExpect(status().isForbidden());
	}

	@Test
	public void handoverCommitUsesAuthenticatedAssigneeInsteadOfSpoofedJjr() throws Exception {
		Fixture fixture = fixture(7);
		Mockito.when(fixture.handoverService.getLeaveHandover(PROCESS, ACTOR))
				.thenReturn(Collections.singletonList(new SmtLeaveHandover()));
		Mockito.when(fixture.handoverService.endLeaveHandover(any(LeaveHandoverDTO.class))).thenReturn(true);
		asAppClient("smart-app");

		fixture.mockMvc.perform(post("/internal/app-leave/handover/commit")
				.header("X-Smart-Actor-Badge", ACTOR)
				.header("X-Smart-Actor-Park-Ids", "7")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", PURPOSE)
				.contentType("application/json")
				.content("{\"processId\":\"P10001\",\"jjr\":\"SPOOFED\"}"))
				.andExpect(status().isOk());

		ArgumentCaptor<LeaveHandoverDTO> command = ArgumentCaptor.forClass(LeaveHandoverDTO.class);
		Mockito.verify(fixture.handoverService).endLeaveHandover(command.capture());
		assertEquals(ACTOR, command.getValue().getJjr());
	}

	@Test
	public void yearHolidayRejectsActorWithoutAnyPark() throws Exception {
		Fixture fixture = fixture(7);
		asAppClient("smart-app");

		fixture.mockMvc.perform(get("/internal/app-leave/year-holiday")
				.header("X-Smart-Actor-Badge", ACTOR)
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", PURPOSE))
				.andExpect(status().isForbidden());
	}

	@Test
	public void recordPageForwardsOnlyAuthenticatedActorParksToService() throws Exception {
		Fixture fixture = fixture(7);
		Mockito.when(fixture.applicationService.getProcessRecord(any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class),
				Mockito.eq(ACTOR), Mockito.eq(0), any(Set.class)))
				.thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<LeaveRecordVO>());
		asAppClient("smart-app");

		fixture.mockMvc.perform(authorisedGet("/internal/app-leave/record/page?current=1&size=10&leaveStatus=0", "7,8"))
				.andExpect(status().isOk());

		org.mockito.ArgumentCaptor<Set> parks = org.mockito.ArgumentCaptor.forClass(Set.class);
		Mockito.verify(fixture.applicationService).getProcessRecord(any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class),
				Mockito.eq(ACTOR), Mockito.eq(0), parks.capture());
		assertEquals(new HashSet<>(java.util.Arrays.asList(7, 8)), parks.getValue());
	}

	@Test
	public void savePersistsActorParkForFollowupApplicationAndHandover() throws Exception {
		SmtLeaveApplicationService applicationService = Mockito.mock(SmtLeaveApplicationService.class);
		ILeaveApplicationService leaveService = Mockito.mock(ILeaveApplicationService.class);
		SmtLeaveHandoverService handoverService = Mockito.mock(SmtLeaveHandoverService.class);
		AtomicReference<SmtLeaveApplication> persisted = new AtomicReference<>();
		Mockito.when(leaveService.saveLeaveApplication(any(com.tce.smart.platform.core.dto.LeaveApplicationDTO.class)))
				.thenAnswer(invocation -> {
					SmtLeaveApplication application = new SmtLeaveApplication();
					org.springframework.beans.BeanUtils.copyProperties(invocation.getArgument(0), application);
					application.setProcessId(PROCESS);
					persisted.set(application);
					return new com.tce.smart.common.core.model.Result<>(true);
				});
		Mockito.when(applicationService.getLeaveApplicationRecord(PROCESS)).thenAnswer(invocation -> persisted.get());
		Mockito.when(handoverService.getLeaveHandover(PROCESS)).thenReturn(Collections.singletonList(new SmtLeaveHandover()));
		InternalAppLeaveController controller = new InternalAppLeaveController(applicationService, leaveService,
				handoverService, new OpenApiAuthenticationAdapter());
		ReflectionTestUtils.setField(controller, "appServiceClientId", "smart-app");
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandlerResolver())
				.build();
		asAppClient("smart-app");

		mockMvc.perform(post("/internal/app-leave/application")
				.header("X-Smart-Actor-Badge", ACTOR)
				.header("X-Smart-Actor-Park-Ids", "7")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", PURPOSE)
				.contentType("application/json")
				.content("{\"badge\":\"A10001\",\"applyBadge\":\"A10001\",\"leaveType\":1,\"leaveReason\":1,\"leaveTime\":\"2026-07-25\",\"leaveStatus\":0}"))
				.andExpect(status().isOk());

		assertEquals(Integer.valueOf(7), persisted.get().getParkId());
		mockMvc.perform(authorisedGet("/internal/app-leave/application/" + PROCESS, "7"))
				.andExpect(status().isOk());
		mockMvc.perform(authorisedGet("/internal/app-leave/handover/" + PROCESS, "7"))
				.andExpect(status().isOk());
	}

	@Test
	public void saveBindsValidatedSelectedParkForMultiParkActor() throws Exception {
		Fixture fixture = fixture(7);
		Mockito.when(fixture.leaveService.saveLeaveApplication(any(com.tce.smart.platform.core.dto.LeaveApplicationDTO.class)))
				.thenReturn(new com.tce.smart.common.core.model.Result<>(true));
		asAppClient("smart-app");

		fixture.mockMvc.perform(authorisedLeaveApplication("7,8")
				.header("X-Smart-Actor-Current-Park-Id", "8"))
				.andExpect(status().isOk());

		ArgumentCaptor<com.tce.smart.platform.core.dto.LeaveApplicationDTO> command = ArgumentCaptor
				.forClass(com.tce.smart.platform.core.dto.LeaveApplicationDTO.class);
		Mockito.verify(fixture.leaveService).saveLeaveApplication(command.capture());
		assertEquals(Integer.valueOf(8), command.getValue().getParkId());
	}

	@Test
	public void saveRejectsMultiParkActorWithoutExplicitSelection() throws Exception {
		Fixture fixture = fixture(7);
		asAppClient("smart-app");

		fixture.mockMvc.perform(authorisedLeaveApplication("7,8"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.msg").value("当前员工关联多个园区，请选择离职园区"));

		Mockito.verify(fixture.leaveService, Mockito.never()).saveLeaveApplication(any());
	}

	@Test
	public void saveRejectsSelectedParkOutsideAuthenticatedActorParks() throws Exception {
		Fixture fixture = fixture(7);
		asAppClient("smart-app");

		fixture.mockMvc.perform(authorisedLeaveApplication("7,8")
				.header("X-Smart-Actor-Current-Park-Id", "9"))
				.andExpect(status().isForbidden());

		Mockito.verify(fixture.leaveService, Mockito.never()).saveLeaveApplication(any());
	}

	private Fixture fixture(int parkId) {
		SmtLeaveApplicationService applicationService = Mockito.mock(SmtLeaveApplicationService.class);
		ILeaveApplicationService leaveService = Mockito.mock(ILeaveApplicationService.class);
		SmtLeaveHandoverService handoverService = Mockito.mock(SmtLeaveHandoverService.class);
		SmtLeaveApplication application = new SmtLeaveApplication();
		application.setParkId(parkId);
		application.setBadge(ACTOR);
		application.setProcessId(PROCESS);
		Mockito.when(applicationService.getLeaveApplicationRecord(PROCESS)).thenReturn(application);
		InternalAppLeaveController controller = new InternalAppLeaveController(applicationService, leaveService,
				handoverService, new OpenApiAuthenticationAdapter());
		ReflectionTestUtils.setField(controller, "appServiceClientId", "smart-app");
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandlerResolver())
				.build();
		return new Fixture(controller, mockMvc, applicationService, leaveService, handoverService);
	}

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authorisedGet(String path, String parkIds) {
		return get(path)
				.header("X-Smart-Actor-Badge", ACTOR)
				.header("X-Smart-Actor-Park-Ids", parkIds)
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", PURPOSE);
	}

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authorisedLeaveApplication(String parkIds) {
		return post("/internal/app-leave/application")
				.header("X-Smart-Actor-Badge", ACTOR)
				.header("X-Smart-Actor-Park-Ids", parkIds)
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", PURPOSE)
				.contentType("application/json")
				.content("{\"badge\":\"A10001\",\"applyBadge\":\"A10001\",\"leaveType\":1,\"leaveReason\":1,\"leaveTime\":\"2026-07-25\",\"leaveStatus\":0}");
	}

	private void asAppClient(String clientId) {
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), clientId, Collections.emptyList(), true,
				Collections.singleton("server"), Collections.emptySet(), null, Collections.emptySet(), Collections.emptyMap());
		SecurityContextHolder.getContext().setAuthentication(new OAuth2Authentication(request, null));
	}

	private static final class Fixture {
		private final InternalAppLeaveController controller;
		private final MockMvc mockMvc;
		private final SmtLeaveApplicationService applicationService;
		private final ILeaveApplicationService leaveService;
		private final SmtLeaveHandoverService handoverService;

		private Fixture(InternalAppLeaveController controller, MockMvc mockMvc, SmtLeaveApplicationService applicationService,
				ILeaveApplicationService leaveService, SmtLeaveHandoverService handoverService) {
			this.controller = controller;
			this.mockMvc = mockMvc;
			this.applicationService = applicationService;
			this.leaveService = leaveService;
			this.handoverService = handoverService;
		}
	}
}
