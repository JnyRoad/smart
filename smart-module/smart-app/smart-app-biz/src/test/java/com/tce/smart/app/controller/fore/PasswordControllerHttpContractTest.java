package com.tce.smart.app.controller.fore;

import com.tce.smart.app.service.fore.PasswordService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 密码找回短信端点的 HTTP 契约测试。
 */
public class PasswordControllerHttpContractTest {

	@Test
	public void passwordRecoveryEndpointsAcceptOnlyPostJsonBodies() throws Exception {
		PasswordService passwordService = Mockito.mock(PasswordService.class);
		Mockito.when(passwordService.createPasswordResetChallenge("8031249")).thenReturn("opaque-challenge");
		Mockito.when(passwordService.sendSmsCode("opaque-challenge")).thenReturn(Boolean.TRUE);
		Mockito.when(passwordService.verifySmsCode("opaque-challenge", "123456")).thenReturn("authorization");
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PasswordController(passwordService)).build();

		mockMvc.perform(post("/password/mobile/query")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"badge\":\"8031249\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(post("/password/sms/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"challengeId\":\"opaque-challenge\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(post("/password/verify")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"challengeId\":\"opaque-challenge\",\"smsCode\":\"123456\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/password/sms/send").param("challengeId", "opaque-challenge"))
				.andExpect(status().isMethodNotAllowed());
		mockMvc.perform(get("/password/mobile/query").param("badge", "8031249"))
				.andExpect(status().isMethodNotAllowed());
		mockMvc.perform(get("/password/verify").param("challengeId", "opaque-challenge").param("smsCode", "123456"))
				.andExpect(status().isMethodNotAllowed());

		Mockito.verify(passwordService).createPasswordResetChallenge("8031249");
		Mockito.verify(passwordService).sendSmsCode("opaque-challenge");
		Mockito.verify(passwordService).verifySmsCode("opaque-challenge", "123456");
	}
}
