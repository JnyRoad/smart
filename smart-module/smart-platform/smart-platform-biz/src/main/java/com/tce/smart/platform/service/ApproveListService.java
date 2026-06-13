package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.RepairsApproveListReqDTO;
import com.tce.smart.platform.api.dto.req.approval.ApproveListQueryDTO;
import com.tce.smart.platform.api.dto.resp.approval.ApprovalProcessRecordReqDTO;
import com.tce.smart.platform.core.dto.RepairsApprovalListDTO;
import com.tce.smart.platform.core.entity.ApproveList;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 待审批表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
public interface ApproveListService extends IService<ApproveList> {

	IPage getApproveList(Page page, ApproveList approveList);

	IPage<RepairsApprovalListDTO> getRepairsApproveList(Page page, RepairsApproveListReqDTO reqDTO);

	IPage getNewPage(Page page, ApproveListQueryDTO queryDTO);

    boolean updateState(ApproveList approveList);

    boolean saveApproveList(ApproveList approveList);

	Integer openNextNode(String businessId, Integer sort, String badge);

	Boolean batchUpdateStatus(String businessId, Integer sort, Integer newStatus);

	List<ApproveList> getByStatus(Integer status, String businessId, Integer sort);

	List<ApproveList> getByType(List<Integer> status, Integer type, String badge);

	/**
	 * 获得审批状态下的节点名称
	 * @param status
	 * @param businessId
	 * @return
	 */
	String getNewApprove(Integer status, String businessId);

	/**
	 * 获得审批流程
	 * @param businessId 事务id
	 * @param applyName 发起人
	 * @param createTime 发起时间
	 * @return
	 */
	List<ApprovalProcessRecordReqDTO> getProcess(String businessId, String applyName, LocalDateTime createTime);

	Integer openNextNode2(String businessId, Integer sort, String badge);

	Integer updateProcessStatus(String businessId, String approveBadge, Integer approvalStatus, Integer approvalListId);

	/**
	 * 根据事务ID获得审批列表
	 * @param businessId
	 * @return
	 */
	List<ApproveList> getByBusinessId(String businessId);
}
