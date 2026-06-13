package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.service.IOARevokeService;
import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.platform.service.SmtProcessRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @ClassName OARevokeController.java
 * @Author fushiping
 * @Description Oa服务撤销controller
 */
@RestController
@RequestMapping("/oa/revoke")
public class OARevokeController extends BaseController{

	@Autowired
	private IOARevokeService ioaRevokeService;

	/**
	 * 接收OA审核信息
	 *
	 * @param processId
	 * @return Result
	 */
	@GetMapping("/process")
	public Result listen(@RequestParam("processId") String processId, @RequestParam("status") String status) {
		String badge = SecurityUtils.getUser().getUsername();
		return success(ioaRevokeService.revokeProcess(Integer.parseInt(processId), badge, status));
	}
}
