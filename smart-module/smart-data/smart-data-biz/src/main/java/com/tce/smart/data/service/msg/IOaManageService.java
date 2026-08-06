package com.tce.smart.data.service.msg;

import com.tce.smart.data.api.dto.msg.req.*;
import com.tce.smart.data.api.dto.msg.resp.OaStaffLookupRespDTO;

/**
 * Oa相关接口服务
 *
 * @author mckaywu
 * @date 2019-06-19 18:15:31
 */
public interface IOaManageService {

	/**
	 * 处理Oa流程审批结束推送消息
	 *
	 * @param flowOverAo
	 * @return
	 */
	Object processFlowOver(FlowOverReqDTO flowOverAo);

	/**
	 * 发送请假申请
	 *
	 * @param sendVacateAo 发送请假申请Ao
	 * @return String 审批流程号
	 */
	String sendVacate(SendVacateReqDTO sendVacateAo);

	/**
	 * 发送调休申请
	 *
	 * @param sendRestAo 发送请假申请Ao
	 * @return String 审批流程号
	 */
	String sendRest(SendRestReqDTO sendRestAo);

	/**
	 * 发送加班申请
	 *
	 * @param sendExtraworkAo 发送加班申请Ao
	 * @return String 审批流程号
	 */
	String sendExtrawork(SendExtraworkReqDTO sendExtraworkAo);

	/**
	 * 发送补卡申请
	 *
	 * @param sendAttendancePatchkAo 发送补卡申请Ao
	 * @return String 审批流程号
	 */
	String sendAttendancePatchk(SendAttendancePatchkAo sendAttendancePatchkAo);

	/**
	 * 发送外宿申请
	 * @param sendOutDormitoryAo
	 * @return
	 */
	String sendOutDormitoryAo(SendOutDormitoryReqDTO sendOutDormitoryAo);

	/**
	 * 离职申请
	 * @param sendLeaveApplicationAo sendLeaveApplicationAo
	 * @return
	 */
	String sendLeaveApplication(SendLeaveApplicationReqDTO sendLeaveApplicationAo);

	/**
	 * 外宿补贴撤销申请
	 * @param sendCallowanceCancelAo
	 * @return
	 */
	String sendCallowanceCancel(SendCallowanceCancelReqDTO sendCallowanceCancelAo);

	/**
	 * OA流程撤销
	 * @param processId 流程id
	 * @param badge 员工工号
	 * @return
	 */
	boolean sendOaRevoke(Integer processId, String badge);

	/**
	 * 发生保密区预约申请
	 *
	 * @param sendSecurityAreaVisitReqDTO 保密区预约
	 * @return String 审批流程号
	 */
	String sendSecurityareaVisit(SendSecurityAreaVisitReqDTO sendSecurityAreaVisitReqDTO);

	/**
	 * 发生保密权限申请
	 *
	 * @param sendSecurityAuthApplyReqDTO 保密权限申请
	 * @return String 审批流程号
	 */
	String sendSecurityAuthApply(SendSecurityAuthApplyReqDTO sendSecurityAuthApplyReqDTO);

	/**
	 * 发送入厂申请
	 *
	 * @param sendEntryFactoryApplyReqDTO 入厂申请
	 * @return String 审批流程号
	 */
	String sendEntryFactoryApply(SendEntryFactoryApplyReqDTO sendEntryFactoryApplyReqDTO);

	/**
	 * 发生放行条申请
	 *
	 * @param sendReleaseApplyReqDTO 放行条申请
	 * @return String 审批流程号
	 */
	String sendReleaseApply(SendReleaseApplyReqDTO sendReleaseApplyReqDTO);

	/**
	 * 发生回写返厂时间
	 *
	 * @param sendWriteBackReturnTimeReqDTO 回写返厂时间
	 * @return String 审批流程号
	 */
	Boolean sendWriteBackReturnTime(SendWriteBackReturnTimeReqDTO sendWriteBackReturnTimeReqDTO);

	/**
	 * 发生保安审批
	 *
	 * @param sendSecurityApprovalReqDTO 保安审批
	 * @return String 审批流程号
	 */
	Boolean sendSecurityApproval(SendSecurityApprovalReqDTO sendSecurityApprovalReqDTO);

	/**
	 * 根据工号查询OA系统员工信息
	 * @param badge
	 * @return
	 */
	OaStaffLookupRespDTO getOAInfoByBadge(String badge);

	/**
	 * 发送合肥园区访客申请
	 * @param sendVisitApplyReqDTO
	 * @return
	 */
	String sendVisitApply(SendVisitApplyReqDTO sendVisitApplyReqDTO);
}
