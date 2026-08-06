package com.tce.smart.data.controller.msg;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.data.api.dto.msg.req.*;
import com.tce.smart.data.api.dto.msg.resp.OaStaffLookupRespDTO;
import com.tce.smart.data.service.msg.IOaManageService;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @ClassName OaDataManagerController.java
 * @Author mingkai.wu
 * @Date 2019-04-29 10:23
 * @Description Oa服务管理controller
 */
@RestController
@AllArgsConstructor
@RequestMapping("/oarmanage")
public class OaDataManageController extends BaseController {

	private IOaManageService appOaManagerService;

	/**
	 * 处理Oa流程审批完结消息推送
	 *
	 * @param flowOverAo 流程信息
	 * @return Result
	 */
	@PostMapping
	public Result save(@RequestBody FlowOverReqDTO flowOverAo) {
		return success(appOaManagerService.processFlowOver(flowOverAo));
	}

	/**
	 * 发送请假申请
	 *
	 * @param sendVacateAo 发送请假申请Ao
	 * @return Result
	 */
	@PostMapping("/send/vacate")
	public Result<?> sendVacate(@RequestBody SendVacateReqDTO sendVacateAo) {
		return success(appOaManagerService.sendVacate(sendVacateAo));
	}

	/**
	 * 发送调休申请
	 *
	 * @param sendRestAo 发送调休申请Ao
	 * @return Result 审批流程号
	 */
	@PostMapping("/send/rest")
	public Result<?> sendRest(@RequestBody SendRestReqDTO sendRestAo) {
		return success(appOaManagerService.sendRest(sendRestAo));
	}

	/**
	 * 发送加班申请
	 *
	 * @param sendExtraworkAo 发送加班申请Ao
	 * @return Result 审批流程号
	 */
	@PostMapping("/send/extrawork")
	public Result<?> sendExtrawork(@RequestBody SendExtraworkReqDTO sendExtraworkAo) {
		return success(appOaManagerService.sendExtrawork(sendExtraworkAo));
	}

	/**
	 * 发送补卡申请
	 *
	 * @param sendAttendancePatchkAo 发送补卡申请Ao
	 * @return Result 审批流程号
	 */
	@PostMapping("/send/attendancepatchk")
	public Result<?> sendAttendancePatchk(@RequestBody SendAttendancePatchkAo sendAttendancePatchkAo) {
		return success(appOaManagerService.sendAttendancePatchk(sendAttendancePatchkAo));
	}

	/**
	 * 发送外宿和外餐申请
	 * @param sendOutDormitoryAo sendOutDormitoryAo
	 * @return
	 */
	@PostMapping("/send/outDormitory")
	public Result<?> sendOutDormitory(@RequestBody SendOutDormitoryReqDTO sendOutDormitoryAo) {
		return success(appOaManagerService.sendOutDormitoryAo(sendOutDormitoryAo));
	}

	/**
	 * 离职申请
	 * @param sendLeaveApplicationAo sendLeaveApplicationAo
	 * @return
	 */
	@PostMapping("/send/leave/application")
	public Result<String> sendLeaveApplication(@RequestBody SendLeaveApplicationReqDTO sendLeaveApplicationAo) {
		return success(appOaManagerService.sendLeaveApplication(sendLeaveApplicationAo));
	}


	/**
	 * 发送外宿补贴撤销申请
	 * @param sendCallowanceCancelReqDTO sendCallowanceCancelAo
	 * @return
	 */
	@PostMapping("/send/callowance/cancel")
	public Result<?> sendCallowanceCancel(@RequestBody SendCallowanceCancelReqDTO sendCallowanceCancelReqDTO) {
		return success(appOaManagerService.sendCallowanceCancel(sendCallowanceCancelReqDTO));
	}

	/**
	 * 发送OA撤销申请
	 * @param processId 流程id
	 * @return
	 */
	@GetMapping("/send/revoke")
	public Result<?> sendOaRevoke(@RequestParam("processId") Integer processId,@RequestParam("badge") String badge) {
		return success(appOaManagerService.sendOaRevoke(processId, badge));
	}

	/**
	 * 发送保密区预约
	 *
	 * @param sendSecurityAreaVisitReqDTO 发送请假申请Ao
	 * @return Result
	 */
	@PostMapping("/send/securityarea/visit")
	public Result<?> sendSecurityareaVisit(@RequestBody SendSecurityAreaVisitReqDTO sendSecurityAreaVisitReqDTO) {
		return success(appOaManagerService.sendSecurityareaVisit(sendSecurityAreaVisitReqDTO));
	}

	/**
	 * 发送保密权限申请
	 *
	 * @param sendSecurityAuthApplyReqDTO 发送保密权限申请Ao
	 * @return Result
	 */
	@PostMapping("/send/security/auth/apply")
	public Result<?> sendSecurityAuthApply(@RequestBody SendSecurityAuthApplyReqDTO sendSecurityAuthApplyReqDTO) {
		return success(appOaManagerService.sendSecurityAuthApply(sendSecurityAuthApplyReqDTO));
	}

	/**
	 * 发送入厂申请
	 *
	 * @param sendEntryFactoryApplyReqDTO 发送入厂申请Ao
	 * @return Result
	 */
	@PostMapping("/send/entry/factory/apply")
	public Result<?> sendEntryFactoryApply(@RequestBody SendEntryFactoryApplyReqDTO sendEntryFactoryApplyReqDTO) {
		return success(appOaManagerService.sendEntryFactoryApply(sendEntryFactoryApplyReqDTO));
	}

	/**
	 * 发送合肥园区访客预约
	 *
	 * @param sendVisitApplyReqDTO
	 * @return Result
	 */
	@PostMapping("/send/hf/visit/apply")
	public Result<?> sendVisitApply(@RequestBody SendVisitApplyReqDTO sendVisitApplyReqDTO) {
		return success(appOaManagerService.sendVisitApply(sendVisitApplyReqDTO));
	}

	/**
	 * 发送放行条申请
	 *
	 * @param sendReleaseApplyReqDTO 发送放行条申请申请Ao
	 * @return Result
	 */
	@PostMapping("/send/release/apply")
	public Result<?> sendReleaseApply(@RequestBody SendReleaseApplyReqDTO sendReleaseApplyReqDTO) {
		return success(appOaManagerService.sendReleaseApply(sendReleaseApplyReqDTO));
	}

	/**
	 * 回写返厂时间
	 *
	 * @param sendWriteBackReturnTimeReqDTO 回写返厂时间Ao
	 * @return Result
	 */
	@PostMapping("/send/write/back/return/time")
	public Result<?> sendWriteBackReturnTime(@RequestBody SendWriteBackReturnTimeReqDTO sendWriteBackReturnTimeReqDTO) {
		return success(appOaManagerService.sendWriteBackReturnTime(sendWriteBackReturnTimeReqDTO));
	}

	/**
	 * 保安审批
	 *
	 * @param sendSecurityApprovalReqDTO 保安审批Ao
	 * @return Result
	 */
	@PostMapping("/send/security/approval")
	public Result<?> sendSecurityApproval(@RequestBody SendSecurityApprovalReqDTO sendSecurityApprovalReqDTO) {
		return success(appOaManagerService.sendSecurityApproval(sendSecurityApprovalReqDTO));
	}

	/**
	 * 根据工号查询OA系统员工信息
	 * @param badge
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/internal/staff/info/{badge}")
	public Result<OaStaffLookupRespDTO> getOAInfoByBadge(@PathVariable("badge") String badge) {
		return success(appOaManagerService.getOAInfoByBadge(badge));
	}
}
