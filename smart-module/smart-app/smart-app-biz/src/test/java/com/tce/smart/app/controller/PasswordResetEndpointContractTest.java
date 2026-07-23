package com.tce.smart.app.controller;

import com.tce.smart.app.controller.fore.PasswordController;
import com.tce.smart.app.service.fore.PasswordService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 公共改密入口必须是 JSON POST，并只委托给内部受管调用链。 */
public class PasswordResetEndpointContractTest {

	@Test
	public void unauthenticatedPasswordResetUsesExactPostJsonRoute() throws Exception {
		PasswordService passwordService = Mockito.mock(PasswordService.class);
		Mockito.when(passwordService.resetPassword(Mockito.any())).thenReturn(Boolean.TRUE);
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PasswordController(passwordService)).build();

		mockMvc.perform(post("/password/update")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"employee\",\"password\":\"NewPassword1!\",\"updateAuthCode\":\"code\"}"))
				.andExpect(status().isOk());
	}
}
