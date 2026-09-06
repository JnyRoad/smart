package com.tce.smart.platform.client.identity;

import java.util.Map;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 内部调用只获得来源标签，不返回身份证号、手机号、组织或密码状态。 */
public class ClientPersonnelCredentialControllerTest {
	@Test
	public void exposesOnlyServerResolvedCredentialSource() throws Exception {
		ClientPersonnelDirectory directory = mock(ClientPersonnelDirectory.class);
		when(directory.credentialSource("E100")).thenReturn("dhr");
		MockMvc mvc = MockMvcBuilders.standaloneSetup(new ClientPersonnelCredentialController(directory)).build();
		mvc.perform(get("/internal/v1/personnel/E100/auth-source"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.source").value("dhr"));
	}
}
