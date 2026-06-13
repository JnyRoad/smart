package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtApprovalCondition;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:24
 */
public interface SmtApprovalConditionService extends IService<SmtApprovalCondition> {

	/**
	 * 根据节点id获得条件
	 * @param nodeId
	 * @return
	 */
	List<SmtApprovalCondition> getList(Integer nodeId);
}
