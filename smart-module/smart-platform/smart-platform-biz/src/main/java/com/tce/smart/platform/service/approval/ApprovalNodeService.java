package com.tce.smart.platform.service.approval;

import com.tce.smart.platform.api.dto.req.approval.ApprovalProcessReqDTO;
import com.tce.smart.platform.api.dto.req.approval.EditApprovalNodeReqDTO;
import com.tce.smart.platform.api.dto.resp.approval.ApproveProcessListReqDTO;

import java.util.List;

/**
 *
 * 审批节点表服务
 *
 * @author fushiping
 * @date 2021-04-08 16:25:18
 */
public interface ApprovalNodeService {

	/**
	 * 保存审批节点
	 * @param editApprovalNode
	 * @return
	 */
	Boolean saveNode(List<EditApprovalNodeReqDTO> editApprovalNode);

	/**
	 * 编辑审批节点
	 * @param editApprovalNode
	 * @return
	 */
	Boolean editNode(List<EditApprovalNodeReqDTO> editApprovalNode);

	Boolean updateNode(List<EditApprovalNodeReqDTO> editApprovalNodes);

	/**
	 * 删除审批节点
	 * @param approvalId
	 * @return
	 */
	Boolean deleteNode(Integer approvalId);

	/**
	 * 根据审批id获得审批流程
	 * @param reqDTO
	 * @return
	 */
	List<ApproveProcessListReqDTO> getApprovalPerson(ApprovalProcessReqDTO reqDTO);

}
