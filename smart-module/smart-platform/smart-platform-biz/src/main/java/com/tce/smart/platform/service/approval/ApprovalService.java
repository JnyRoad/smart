package com.tce.smart.platform.service.approval;

import com.tce.smart.platform.api.dto.req.approval.ApprovalProcessReqDTO;
import com.tce.smart.platform.api.dto.resp.approval.ApproveProcessListReqDTO;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:32
 */
public interface ApprovalService  {

	/**
	 * 获得物品放行审批流程
	 * @param reqDTO
	 * @return
	 */
	List<ApproveProcessListReqDTO> approvalProcess(ApprovalProcessReqDTO reqDTO);

}
