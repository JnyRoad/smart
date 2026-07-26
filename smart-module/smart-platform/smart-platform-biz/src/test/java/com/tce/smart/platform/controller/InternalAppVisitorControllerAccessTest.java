package com.tce.smart.platform.controller;

import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.vo.SearchAppVisitorDetailVO;
import com.tce.smart.platform.service.SmtVisitorService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 访客自助详情必须同时绑定精确服务调用方、记录归属和登录员工所属园区。
 */
public class InternalAppVisitorControllerAccessTest {
	private static final String ACTOR_BADGE = "A10001";
	private static final String PURPOSE = "app-visitor-self";

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void detailRejectsActorOutsideVisitorPark() throws Exception {
		MockMvc mockMvc = mockMvc(visitorInPark(7));
		asAppClient();

		mockMvc.perform(authorisedDetail(7, "8"))
				.andExpect(status().isForbidden());
	}

	@Test
	public void detailReturnsOnlyMinimumMaskedFieldsForOwnerInSamePark() throws Exception {
		MockMvc mockMvc = mockMvc(visitorInPark(7));
		asAppClient();

		mockMvc.perform(authorisedDetail(7, "7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.visitorName").value("访客甲"))
				.andExpect(jsonPath("$.data.visitorPhone").value("138****8000"))
				.andExpect(jsonPath("$.data.receptionistPhone").value("139****9000"))
				.andExpect(jsonPath("$.data.certNo").doesNotExist())
				.andExpect(jsonPath("$.data.visitorFrontPhoto").doesNotExist())
				.andExpect(jsonPath("$.data.visitorBackPhoto").doesNotExist())
				.andExpect(jsonPath("$.data.tripCode").doesNotExist())
				.andExpect(jsonPath("$.data.healthcode").doesNotExist())
				.andExpect(jsonPath("$.data.processId").doesNotExist())
				.andExpect(jsonPath("$.data.processList").doesNotExist());
	}

	@Test
	public void detailRejectsDifferentActorEvenWhenParkMatches() throws Exception {
		MockMvc mockMvc = mockMvc(visitorInPark(7));
		asAppClient();

		mockMvc.perform(get("/internal/app-visitor/detail/7")
				.header("X-Smart-Actor-Badge", "A10002")
				.header("X-Smart-Actor-Park-Ids", "7")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", PURPOSE))
				.andExpect(status().isForbidden());
	}

	@Test
	public void detailRejectsWrongClientSourcePurposeAndEmptyClientConfiguration() throws Exception {
		MockMvc mockMvc = mockMvc(visitorInPark(7));
		asAppClient("other-client");
		mockMvc.perform(authorisedDetail(7, "7")).andExpect(status().isForbidden());

		asAppClient("smart-app");
		mockMvc.perform(get("/internal/app-visitor/detail/7")
				.header("X-Smart-Actor-Badge", ACTOR_BADGE)
				.header("X-Smart-Actor-Park-Ids", "7")
				.header(SecurityConstants.FROM, "external")
				.header("X-Smart-Internal-Purpose", PURPOSE))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/internal/app-visitor/detail/7")
				.header("X-Smart-Actor-Badge", ACTOR_BADGE)
				.header("X-Smart-Actor-Park-Ids", "7")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", "other-purpose"))
				.andExpect(status().isForbidden());

		SmtVisitorService service = Mockito.mock(SmtVisitorService.class);
		InternalAppVisitorController controller = new InternalAppVisitorController(service,
				new OpenApiAuthenticationAdapter());
		ReflectionTestUtils.setField(controller, "appServiceClientId", "");
		MockMvc emptyConfigMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandlerResolver()).build();
		emptyConfigMvc.perform(authorisedDetail(7, "7")).andExpect(status().isForbidden());
	}

	private MockMvc mockMvc(SmtVisitor visitor) {
		SmtVisitorService service = Mockito.mock(SmtVisitorService.class);
		Mockito.when(service.getById(7L)).thenReturn(visitor);
		Mockito.when(service.searchAppVisitorDetailById(7L)).thenReturn(detail());
		InternalAppVisitorController controller = new InternalAppVisitorController(service,
				new OpenApiAuthenticationAdapter());
		ReflectionTestUtils.setField(controller, "appServiceClientId", "smart-app");
		return MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandlerResolver())
				.build();
	}

	private SmtVisitor visitorInPark(int parkId) {
		SmtVisitor visitor = new SmtVisitor();
		visitor.setParkId(parkId);
		visitor.setPromoterBadge(ACTOR_BADGE);
		return visitor;
	}

	private SearchAppVisitorDetailVO detail() {
		SearchAppVisitorDetailVO detail = new SearchAppVisitorDetailVO();
		detail.setVisitorId(7L);
		detail.setVisitorName("访客甲");
		detail.setVisitorPhone("13812348000");
		detail.setReceptionistPhone("13912349000");
		detail.setCertNo("440101199001010011");
		detail.setVisitorFrontPhoto("front-photo");
		detail.setVisitorBackPhoto("back-photo");
		detail.setTripCode("trip-code");
		detail.setHealthcode("health-code");
		detail.setProcessId("process-id");
		return detail;
	}

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authorisedDetail(long id, String parkIds) {
		return get("/internal/app-visitor/detail/{id}", id)
				.header("X-Smart-Actor-Badge", ACTOR_BADGE)
				.header("X-Smart-Actor-Park-Ids", parkIds)
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", PURPOSE);
	}

	private void asAppClient() {
		asAppClient("smart-app");
	}

	private void asAppClient(String clientId) {
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), clientId, Collections.emptyList(), true,
				Collections.singleton("server"), Collections.emptySet(), null, Collections.emptySet(), Collections.emptyMap());
		SecurityContextHolder.getContext().setAuthentication(new OAuth2Authentication(request, null));
	}
}
