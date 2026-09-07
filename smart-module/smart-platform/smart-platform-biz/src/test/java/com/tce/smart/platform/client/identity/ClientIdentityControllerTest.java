package com.tce.smart.platform.client.identity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** App 工作台只接收服务端白名单内、当前身份已获授权的模块元数据。 */
public class ClientIdentityControllerTest {
	@Test
	public void appsUsesDedicatedCurrentIdentityRoute() throws Exception {
		ClientIdentityService service = mock(ClientIdentityService.class);
		Map<String, Object> item = new LinkedHashMap<>();
		item.put("id", "item-pass-apply");
		item.put("permission", "item-pass:apply");
		when(service.apps()).thenReturn(Collections.singletonList(item));
		MockMvc mvc = MockMvcBuilders.standaloneSetup(new ClientIdentityController(service))
				.setControllerAdvice(new ClientIdentityExceptionHandler()).build();

		mvc.perform(get("/api/v1/me/apps"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("item-pass-apply"))
				.andExpect(jsonPath("$[0].permission").value("item-pass:apply"));
	}
}
