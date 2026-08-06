package com.tce.smart.app.controller.wechat;

import com.tce.smart.app.service.fore.impl.VisitorServiceImpl;
import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.platform.api.feign.RemoteSmtImageService;
import com.tce.smart.platform.api.feign.RemoteVisitorService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 真实 HTTP 控制器链路回归：Nacos 放行只允许请求抵达，不能使匿名调用绕过 action capability。
 */
public class WechatVisitAnonymousActionHttpTest {

	private RemoteVisitorService visitorRemote;
	private RemoteSmtImageService imageRemote;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		visitorRemote = Mockito.mock(RemoteVisitorService.class);
		imageRemote = Mockito.mock(RemoteSmtImageService.class);
		VisitorServiceImpl visitorService = new VisitorServiceImpl(visitorRemote, null, null, imageRemote,
				null, null, null, null, null, null);
		mockMvc = MockMvcBuilders.standaloneSetup(new WechatVisitController(visitorService))
				.setControllerAdvice(new GlobalExceptionHandlerResolver())
				.build();
	}

	@Test
	public void anonymousFaceUploadWithoutCapabilityIsForbiddenBeforeStorage() throws Exception {
		mockMvc.perform(post("/wechat/visit/checkFace")
				.contentType("application/json")
				.content("{\"visitorPhoto\":\"base64-image\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(1));
		Mockito.verifyZeroInteractions(visitorRemote, imageRemote);
	}

	@Test
	public void anonymousBlacklistCheckWithOnlyOneHeaderIsForbiddenBeforeQuery() throws Exception {
		mockMvc.perform(post("/wechat/visit/checkBlackVisitor")
				.header("X-Visitor-Action-Capability", "ticket-without-draft")
				.contentType("application/json")
				.content("{}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(1));
		Mockito.verifyZeroInteractions(visitorRemote, imageRemote);
	}
}
