package com.tce.smart.platform.controller;

import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.platform.service.oacallback.DispatchResult;
import com.tce.smart.platform.service.oacallback.OaCallbackDispatcher;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/** OA 回调入口：断言真实 HTTP status（spec §4，防被全局异常处理器吞成 200） */
public class OAWorkflowControllerTest {

	private OaCallbackDispatcher dispatcher;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		dispatcher = mock(OaCallbackDispatcher.class);
		OAWorkflowController controller = new OAWorkflowController(dispatcher, mock(IOAWorkflowService.class));
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	public void allSuccess_http200() throws Exception {
		when(dispatcher.dispatch(any())).thenReturn(
				DispatchResult.builder().allSuccess(true).failedHandlers(Collections.emptyList()).build());
		mockMvc.perform(post("/oa/workflow/over").contentType(APPLICATION_JSON)
				.content("{\"requestid\":\"28753680\"}"))
				.andExpect(status().isOk());
	}

	@Test
	public void partialFail_http500() throws Exception {
		when(dispatcher.dispatch(any())).thenReturn(
				DispatchResult.builder().allSuccess(false)
						.failedHandlers(Collections.singletonList("askLeave")).build());
		mockMvc.perform(post("/oa/workflow/over").contentType(APPLICATION_JSON)
				.content("{\"requestid\":\"28753680\"}"))
				.andExpect(status().isInternalServerError());
	}
}
