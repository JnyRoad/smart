package com.tce.smart.data.api.feign.msg;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.msg.req.*;
import com.tce.smart.data.api.vo.msg.QueryOaStaffRespVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Oa工作审批创建服务
 *
 * @author mingkai.wu
 * @date 2019-05-15 18:54:18
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteOaWorkFlowService {

	/**
	 * 创建OA请假审批流
	 *
	 * @param sendVacateAo Oa请假审批EHR表字段字段
	 * @return 审批流程号
	 */
	@PostMapping("/oarmanage/send/vacate")
	Result<String> sendVacateAo(@RequestBody SendVacateReqDTO sendVacateAo);

	/**
	 * 创建OA加班审批流
	 *
	 * @param sendExtraworkAo Oa加班审批EHR表字段字段
	 * @return 审批流程号
	 */
	@PostMapping("/oarmanage/send/extrawork")
	Result<String> sendExtrawork(@RequestBody SendExtraworkReqDTO sendExtraworkAo);

	/**
	 * 创建OA调休审批流
	 *
	 * @param sendRestAo Oa请假审批EHR表字段字段
	 * @return 审批流程号
	 */
	@PostMapping("/oarmanage/send/rest")
	Result<String> sendRest(@RequestBody SendRestReqDTO sendRestAo);

	/**
	 * 创建OA外宿审批流
	 * @param sendOutDormitoryAo oa外宿审批EHR表字段
	 * @return 审批流程号
	 */
	@PostMapping("/oarmanage/send/outDormitory")
	Result<String> sendOutDormitory(@RequestBody SendOutDormitoryReqDTO sendOutDormitoryAo);

	/**
	 * 创建OA补卡审批流
	 *
	 * @param sendAttendancePatchkAo Oab补卡审批EHR表字段字段
	 * @return 审批流程号
	 */
	@PostMapping("/oarmanage/send/attendancepatchk")
	Result<String> sendRest(@RequestBody SendAttendancePatchkAo sendAttendancePatchkAo);

	/**
	 * 离职申请
	 * @param sendLeaveApplicationAo 申请参数
	 * @return 审批流程号
	 */
	@PostMapping("/oarmanage/send/leave/application")
    Result<String> sendLeaveApplication(@RequestBody SendLeaveApplicationReqDTO sendLeaveApplicationAo);



	/**
	 * 外宿补贴撤销申请
	 * @param sendCallowanceCancelReqDTO 申请参数
	 * @return 审批流程号
	 */
	@PostMapping("/oarmanage/send/callowance/cancel")
    Result<String> sendCallowanceCancel(@RequestBody SendCallowanceCancelReqDTO sendCallowanceCancelReqDTO);

	/**
	 * 发送OA撤销申请
	 * @param processId 流程id
	 * @return
	 */
	@GetMapping("/oarmanage/send/revoke")
    Result<Boolean> sendOaRevoke(@RequestParam("processId") Integer processId, @RequestParam("badge") String badge);

	/**
	 * 保密区预约申请
	 * @param sendSecurityAreaVisitReqDTO 申请参数
	 * @return 审批流程号
	 */
	@PostMapping("/oarmanage/send/securityarea/visit")
    Result<String> sendSecurityAreaVisit(@RequestBody SendSecurityAreaVisitReqDTO sendSecurityAreaVisitReqDTO);


	/**
	 * 保密权限申请
	 * @param sendSecurityAuthApplyReqDTO 申请参数
	 * @return 审批流程号
	 */
	@PostMapping("/oarmanage/send/security/auth/apply")
    Result<String> sendSecurityAuthApply(@RequestBody SendSecurityAuthApplyReqDTO sendSecurityAuthApplyReqDTO);

	/**
	 * 入厂申请
	 * @param sendEntryFactoryApplyReqDTO 申请参数
	 * @return 审批流程号
	 */
	@PostMapping("/oarmanage/send/entry/factory/apply")
    Result<String> sendEntryFactoryApply(@RequestBody SendEntryFactoryApplyReqDTO sendEntryFactoryApplyReqDTO);

	/**
	 * 合肥访客预约
	 * @param sendEntryFactoryApplyReqDTO
	 * @return
	 */
	@PostMapping("/oarmanage/send/hf/visit/apply")
	Result<String> sendVisitApply(@RequestBody SendVisitApplyReqDTO sendEntryFactoryApplyReqDTO);

	/**
	 * 放行条申请
	 * @param sendReleaseApplyReqDTO 申请参数
	 * @return 审批流程号
	 */
	@PostMapping("/oarmanage/send/release/apply")
    Result<String> sendReleaseApply(@RequestBody SendReleaseApplyReqDTO sendReleaseApplyReqDTO);

	/**
	 * 回写返厂时间申请
	 * @param sendWriteBackReturnTimeReqDTO 申请参数
	 * @return 审批流程号
	 */
	@PostMapping("/oarmanage/send/write/back/return/time")
    Result<Boolean> sendWriteBackReturnTime(@RequestBody SendWriteBackReturnTimeReqDTO sendWriteBackReturnTimeReqDTO);

	/**
	 * 保安审批申请
	 * @param sendSecurityApprovalReqDTO 申请参数
	 * @return 审批流程号
	 */
	@PostMapping("/oarmanage/send/security/approval")
    Result<Boolean> sendSecurityApproval(@RequestBody SendSecurityApprovalReqDTO sendSecurityApprovalReqDTO);

	/**
	 * 根据工号查询OA系统员工信息
	 * @param badge
	 * @return
	 */
	@GetMapping("/oarmanage/staff/info/{badge}")
	Result<QueryOaStaffRespVo> getOAInfoByBadge(@PathVariable("badge") String badge);
}
