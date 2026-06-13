package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtApprovalNode;

import java.util.List;

/**
 *
 * 审批节点服务
 * @author fushiping
 * @date 2021-04-08 16:25:18
 */
public interface SmtApprovalNodeService extends IService<SmtApprovalNode> {

	/**
	 * 跟进审批id获得节点列表
	 * @param approvalId
	 * @return
	 */
	List<SmtApprovalNode> getList(Integer approvalId);

}
