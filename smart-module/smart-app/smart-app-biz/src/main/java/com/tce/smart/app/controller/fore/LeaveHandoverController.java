package com.tce.smart.app.controller.fore;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.app.service.fore.LeaveHandoverService;
import com.tce.smart.app.vo.fore.LeaveHandoverVO;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import lombok.AllArgsConstructor;

/**
 * 工作交接控制器
 * @author 王艳勇
 *
 */

@RestController
@AllArgsConstructor
public class LeaveHandoverController extends BaseController{

    private LeaveHandoverService leaveHandoverService;

	/**
     * 获取交接内容
     * @param processId 流程编号
     * @return
     */
	@GetMapping("/application/approve/workHand/get/{processId}")
	public Result<?> getLeaveHandoverByJjr(@PathVariable(value="processId",required = true)String processId) {
	    return leaveHandoverService.getLeaveHandoverByJjr(processId);
	}

    /**
     * 确认工作交接
     * @param leaveHandoverVO
     * @return
     */
	@PostMapping("/application/approve/workHand/submit")
	public Result<?> endLeaveHandover(@RequestBody LeaveHandoverVO leaveHandoverVO) {
	    return leaveHandoverService.endLeaveHandover(leaveHandoverVO);
	}

	/**
     * 开始工作交接
     * @param processId 流程编号
     * @return
     */
	@GetMapping("/application/approve/start/{processId}")
	public Result<?> startLeaveHandover(@PathVariable(value="processId",required = true)String processId) {
	    return leaveHandoverService.startLeaveHandover(processId);
	}

	/**
     * 提交工作交接
     * @param processId 流程编号
     * @return
     */
	@GetMapping("/application/approve/commit/{processId}")
	public Result<?> commitLeaveHandover(@PathVariable(value="processId",required = true)String processId) {
	    return leaveHandoverService.commitLeaveHandover(processId);
	}


}
