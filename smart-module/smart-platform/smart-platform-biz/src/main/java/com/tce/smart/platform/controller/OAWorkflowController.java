package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.platform.service.oacallback.DispatchResult;
import com.tce.smart.platform.service.oacallback.OaCallbackDispatcher;
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

	public OAWorkflowController(OaCallbackDispatcher dispatcher, IOAWorkflowService iOAWorkflowService) {
		this.dispatcher = dispatcher;
		this.iOAWorkflowService = iOAWorkflowService;
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
}
