package com.tce.smart.platform.client.release;

import com.tce.smart.platform.client.identity.ClientApiException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** App 放行 HTTP 合同不接受伪造字段，错误正文也不得回显封条或卡号。 */
public class ClientReleaseControllerTest {
	@Test
	public void detailUsesTheDedicatedItemPassRoute() throws Exception {
		ClientReleaseService service = mock(ClientReleaseService.class);
		Map<String, Object> detail = new LinkedHashMap<>();
		detail.put("id", "REL-1");
		when(service.detail("REL-1")).thenReturn(detail);
		MockMvc mvc = MockMvcBuilders.standaloneSetup(new ClientReleaseController(service))
				.setControllerAdvice(new ClientReleaseExceptionHandler()).build();

		mvc.perform(get("/api/v1/item-passes/REL-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("REL-1"));
	}

	@Test
	public void exactApplicationPayloadAndSafeErrorsAreEnforced() throws Exception {
		ClientReleaseService service = mock(ClientReleaseService.class);
		Map<String, Object> created = new LinkedHashMap<>(); created.put("id", "REL-1");
		when(service.create(any(ClientReleaseRequests.Application.class), eq("op-1"))).thenReturn(created);
		MockMvc mvc = MockMvcBuilders.standaloneSetup(new ClientReleaseController(service))
				.setControllerAdvice(new ClientReleaseExceptionHandler()).build();
		String accepted = "{\"title\":\"图纸\",\"reason\":\"交接\",\"fromPostId\":\"A\",\"toPostId\":\"B\",\"supplierName\":\"\",\"visitorName\":\"\",\"materials\":\"图纸\",\"seals\":[\"SEAL-1\"]}";
		mvc.perform(post("/api/v1/item-passes").header("Idempotency-Key", "op-1").contentType("application/json").content(accepted))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value("REL-1"));
		mvc.perform(post("/api/v1/item-passes").header("Idempotency-Key", "op-1").contentType("application/json")
				.content("{\"kind\":\"item-pass\",\"title\":\"图纸\",\"reason\":\"交接\",\"fromPostId\":\"A\",\"toPostId\":\"B\",\"supplierName\":\"\",\"visitorName\":\"\",\"materials\":\"图纸\",\"seals\":[\"SEAL-1\"]}"))
				.andExpect(status().isBadRequest());
		String leaked = mvc.perform(post("/api/v1/item-passes").header("Idempotency-Key", "op-1").contentType("application/json")
				.content("{\"title\":\"图纸\",\"reason\":\"交接\",\"fromPostId\":\"A\",\"toPostId\":\"B\",\"supplierName\":\"\",\"visitorName\":\"\",\"materials\":\"图纸\",\"seals\":[\"SEAL-SECRET\"],\"actorId\":\"forged\"}"))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
		Assert.assertFalse(leaked.contains("SEAL-SECRET"));
	}

	@Test
	public void applicationAcceptsTheDocumentedReasonAndMaterialsLengths() throws Exception {
		ClientReleaseService service = mock(ClientReleaseService.class);
		when(service.create(any(ClientReleaseRequests.Application.class), anyString())).thenReturn(Collections.singletonMap("id", "REL-2"));
		MockMvc mvc = MockMvcBuilders.standaloneSetup(new ClientReleaseController(service))
				.setControllerAdvice(new ClientReleaseExceptionHandler()).build();
		String reason = String.join("", Collections.nCopies(500, "原"));
		String materials = String.join("", Collections.nCopies(1000, "料"));
		String payload = "{\"title\":\"图纸\",\"reason\":\"" + reason + "\",\"fromPostId\":\"A\",\"toPostId\":\"B\",\"supplierName\":\"\",\"visitorName\":\"\",\"materials\":\"" + materials + "\",\"seals\":[]}";
		mvc.perform(post("/api/v1/item-passes").header("Idempotency-Key", "length-accepted").contentType("application/json").content(payload))
				.andExpect(status().isOk());
		String tooLongReason = String.join("", Collections.nCopies(501, "原"));
		mvc.perform(post("/api/v1/item-passes").header("Idempotency-Key", "length-rejected").contentType("application/json")
				.content(payload.replace(reason, tooLongReason))).andExpect(status().isBadRequest());
	}

	@Test
	public void actionFailuresUseFixedResponses() throws Exception {
		ClientReleaseService service = mock(ClientReleaseService.class);
		when(service.action(eq("REL-1"), any(ClientReleaseRequests.Action.class), eq("op-1"))).thenThrow(new ClientApiException(403));
		MockMvc mvc = MockMvcBuilders.standaloneSetup(new ClientReleaseController(service))
				.setControllerAdvice(new ClientReleaseExceptionHandler()).build();
		String response = mvc.perform(post("/api/v1/item-passes/REL-1/actions").header("Idempotency-Key", "op-1")
				.contentType("application/json").content("{\"action\":\"approve\",\"postId\":\"\",\"comment\":\"secret-comment\"}"))
				.andExpect(status().isForbidden()).andReturn().getResponse().getContentAsString();
		Assert.assertFalse(response.contains("secret-comment"));
	}
}
