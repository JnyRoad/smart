package com.tce.smart.platform.controller;

import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.platform.service.oacallback.DispatchResult;
import com.tce.smart.platform.service.oacallback.OaCallbackDispatcher;
import com.tce.smart.platform.service.oacallback.OaCallbackReplayService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/** OA 回调入口：断言真实 HTTP status（spec §4，防被全局异常处理器吞成 200） */
public class OAWorkflowControllerTest {

	private OaCallbackDispatcher dispatcher;
	private com.tce.smart.platform.service.oacallback.OaCallbackLogService logService;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		dispatcher = mock(OaCallbackDispatcher.class);
		logService = mock(com.tce.smart.platform.service.oacallback.OaCallbackLogService.class);
		OAWorkflowController controller = new OAWorkflowController(dispatcher, mock(IOAWorkflowService.class),
				mock(OaCallbackReplayService.class), logService);
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

	/** 清理端点必须 @Inner：仅供 smart-schedule 定时任务调用 */
	@Test
	public void cleanExpiredLogs_hasInnerAnnotation() throws Exception {
		Method method = OAWorkflowController.class.getMethod("cleanExpiredLogs");
		assertNotNull("/callback/log/clean 缺少 @Inner 注解", method.getAnnotation(Inner.class));
	}

	/** 清理端点调用 service 并返回 200 */
	@Test
	public void cleanExpiredLogs_invokesServiceAndReturns200() throws Exception {
		when(logService.cleanExpiredLogs()).thenReturn(3);
		mockMvc.perform(get("/oa/workflow/callback/log/clean"))
				.andExpect(status().isOk());
		verify(logService).cleanExpiredLogs();
	}
}
