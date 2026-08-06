package com.tce.smart.app.controller;

import com.tce.smart.app.service.AppSmsService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 访客短信 HTTP 契约：外部请求只能使用场景明确的 POST JSON，手机号和验证码不得落入 URL。
 */
public class AppSmsControllerHttpContractTest {

	@Test
	public void visitorSmsEndpointsAcceptOnlyPostJsonBodies() throws Exception {
		AppSmsService smsService = Mockito.mock(AppSmsService.class);
		Mockito.when(smsService.sendVisitorSmsCode("13800138000")).thenReturn(Boolean.TRUE);
		Mockito.when(smsService.sendLoginSmsCode("13800138000")).thenReturn(Boolean.TRUE);
		Mockito.when(smsService.verifyVisitorSmsCode("13800138000", "123456")).thenReturn(Boolean.TRUE);
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AppSmsController(smsService)).build();

		mockMvc.perform(post("/sms/visitor/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"mobile\":\"13800138000\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(post("/sms/visitor/verify")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"mobile\":\"13800138000\",\"smsCode\":\"123456\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(post("/sms/login/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"mobile\":\"13800138000\"}"))
				.andExpect(status().isOk());

		// 旧 GET 映射已彻底移除：404 是 fail-closed，不允许兼容回退到会泄露 URL 参数的处理器。
		mockMvc.perform(get("/sms/send/13800138000"))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/sms/send/getCode/13800138000"))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/sms/verify").param("mobile", "13800138000").param("smsCode", "123456"))
				.andExpect(status().isNotFound());

		Mockito.verify(smsService).sendVisitorSmsCode("13800138000");
		Mockito.verify(smsService).sendLoginSmsCode("13800138000");
		Mockito.verify(smsService).verifyVisitorSmsCode("13800138000", "123456");
	}
}
