package com.tce.smart.auth.client.session;

import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Collections;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** HTTP 契约拒绝扩展字段和畸形正文，且错误信息不回显密码。 */
public class ClientSessionControllerTest {
	@Test
	public void loginAcceptsOnlyExactJsonShapeAndHidesCredentialFailures() throws Exception {
		ClientSessionService service = mock(ClientSessionService.class);
		when(service.login("E100", "pass-1")).thenReturn(new java.util.LinkedHashMap<String, Object>() {{
			put("token", "token-1"); put("expiresAt", 1770000000000L);
		}});
		MockMvc mvc = MockMvcBuilders.standaloneSetup(new ClientSessionController(service))
				.setControllerAdvice(new ClientSessionExceptionHandler()).build();
		mvc.perform(post("/api/v1/sessions").contentType("application/json")
				.content("{\"staffNo\":\"E100\",\"password\":\"pass-1\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.token").value("token-1"));
		mvc.perform(post("/api/v1/sessions").contentType("application/json")
				.content("{\"staffNo\":\"E100\",\"staffNo\":\"E101\",\"password\":\"pass-1\"}"))
				.andExpect(status().isBadRequest());
		String rejected = mvc.perform(post("/api/v1/sessions").contentType("application/json")
				.content("{\"staffNo\":\"E100\",\"password\":\"pass-1\",\"role\":\"admin\"}"))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
		org.junit.Assert.assertFalse(rejected.contains("pass-1"));
		when(service.login("E100", "bad-pass")).thenThrow(new ClientSessionException(401));
		String failed = mvc.perform(post("/api/v1/sessions").contentType("application/json")
				.content("{\"staffNo\":\"E100\",\"password\":\"bad-pass\"}"))
				.andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
		org.junit.Assert.assertFalse(failed.contains("bad-pass"));
	}
}
