package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.platform.service.SmtProcessRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @ClassName OaProcessListenerController.java
 * @Author mingkai.wu
 * @Date 2019-04-29 10:23
 * @Description Oa服务管理controller
 */
@RestController
@RequestMapping("/oa/workflow")
public class OAWorkflowController extends BaseController{
	@Autowired
	private SmtProcessRecordService processRecordService;
	@Autowired
	private IOAWorkflowService iOAWorkflowService;

	/**
	 * 接收OA审核信息
	 *
	 * @param workFlowAO
	 * @return Result
	 */
	@PostMapping("/over")
	public Result listen(@RequestBody WorkFlowAO workFlowAO) {
		processRecordService.saveProcessRecord(workFlowAO);
		return success();
	}

	/**
	 * 根据审批编号查询OA审批记录
	 *
	 * @param requestId
	 * @return Result
	 */
	@GetMapping("/query")
	public Result query(@RequestParam("requestId") String requestId) {
		return success(iOAWorkflowService.query(requestId));
	}

}
