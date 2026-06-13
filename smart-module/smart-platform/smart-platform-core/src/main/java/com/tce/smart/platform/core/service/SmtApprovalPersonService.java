package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtApprovalPerson;
import io.swagger.models.auth.In;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:00
 */
public interface SmtApprovalPersonService extends IService<SmtApprovalPerson> {

	/**
	 * 根据节点id获得审批人员
	 * @param nodeId
	 * @return
	 */
	List<SmtApprovalPerson> getList(Integer nodeId);

}
