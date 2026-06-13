package com.tce.smart.platform.service;

import com.tce.smart.platform.core.ao.LeaveApplyAO;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;

/**
 *
 * @ClassName AppOaManagerService.java
 * @Author mingkai.wu
 * @Date 2019-04-29 09:43
 * @Description
 */
public interface IOAWorkflowService {

	/**
	 * 向OA推送离职审批申请
	 *
	 * @param
	 * @return
	 */
	String leaveApply(LeaveApplyAO leaveApplyAO);

	/**
	 * 根据审批编号查询OA审批记录
	 *
	 * @param
	 * @return
	 */
	WorkFlowLogDTO query(String requestId);
}
