package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.platform.service.oacallback.DispatchResult;
import com.tce.smart.platform.service.oacallback.OaCallbackDispatcher;
import com.tce.smart.platform.service.oacallback.OaCallbackLogService;
import com.tce.smart.platform.service.oacallback.OaCallbackReplayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OA 服务管理 controller：接收 OA 审批回调并分发给各业务 handler。
 */
@RestController
@RequestMapping("/oa/workflow")
public class OAWorkflowController extends BaseController {

	private final OaCallbackDispatcher dispatcher;
	private final IOAWorkflowService iOAWorkflowService;
	private final OaCallbackReplayService replayService;
	private final OaCallbackLogService logService;

	public OAWorkflowController(OaCallbackDispatcher dispatcher, IOAWorkflowService iOAWorkflowService,
			OaCallbackReplayService replayService, OaCallbackLogService logService) {
		this.dispatcher = dispatcher;
		this.iOAWorkflowService = iOAWorkflowService;
		this.replayService = replayService;
		this.logService = logService;
	}

	/**
	 * 接收 OA 审核回调。
	 * 注意：存在处理失败时必须返回真实 HTTP 500（Result.fail/全局异常处理器均为 HTTP 200，
	 * 不能触发 OA 重试，spec §3.2.2 四审 Medium）。
	 */
	@PostMapping("/over")
	public ResponseEntity<Result> listen(@RequestBody WorkFlowAO workFlowAO) {
		DispatchResult result = dispatcher.dispatch(workFlowAO);
		if (result.isAllSuccess()) {
			return ResponseEntity.ok(success());
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(fail("部分业务处理失败：" + String.join(",", result.getFailedHandlers())));
	}

	/** 根据审批编号查询 OA 审批记录（原样保留） */
	@GetMapping("/query")
	public Result query(@RequestParam("requestId") String requestId) {
		return success(iOAWorkflowService.query(requestId));
	}

	/** 按日志 id 重放失败的回调处理（仅内部调用，spec §3.3） */
	@Inner
	@PostMapping("/replay/{logId}")
	public Result replay(@PathVariable("logId") Long logId) {
		return replayService.replay(logId);
	}

	/** 过期回调日志清理（90 天整行删，仅供 smart-schedule 定时任务调用，spec 2026-07-05 §3.2） */
	@Inner
	@OpenApi("server")
	@GetMapping("/callback/log/clean")
	public Result cleanExpiredLogs() {
		return success(logService.cleanExpiredLogs());
	}
}
