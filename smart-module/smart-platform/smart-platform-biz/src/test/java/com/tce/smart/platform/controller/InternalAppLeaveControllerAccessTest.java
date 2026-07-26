package com.tce.smart.platform.controller;

import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.platform.core.dto.LeaveHandoverDTO;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.entity.SmtLeaveHandover;
import com.tce.smart.platform.core.service.SmtLeaveApplicationService;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
		return new Fixture(controller, mockMvc, handoverService);
	}

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authorisedGet(String path, String parkIds) {
		return get(path)
				.header("X-Smart-Actor-Badge", ACTOR)
				.header("X-Smart-Actor-Park-Ids", parkIds)
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", PURPOSE);
	}

	private void asAppClient(String clientId) {
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), clientId, Collections.emptyList(), true,
				Collections.singleton("server"), Collections.emptySet(), null, Collections.emptySet(), Collections.emptyMap());
		SecurityContextHolder.getContext().setAuthentication(new OAuth2Authentication(request, null));
	}

	private static final class Fixture {
		private final InternalAppLeaveController controller;
		private final MockMvc mockMvc;
		private final SmtLeaveHandoverService handoverService;

		private Fixture(InternalAppLeaveController controller, MockMvc mockMvc, SmtLeaveHandoverService handoverService) {
			this.controller = controller;
			this.mockMvc = mockMvc;
			this.handoverService = handoverService;
		}
	}
}
