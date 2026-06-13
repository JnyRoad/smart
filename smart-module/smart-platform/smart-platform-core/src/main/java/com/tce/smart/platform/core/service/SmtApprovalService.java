package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtApproval;

/**
 *
 *审批配置表
 * @author fushiping
 * @date 2021-04-08 16:25:32
 */
public interface SmtApprovalService extends IService<SmtApproval> {

	/**
	 * 保存审批配置
	 * @param smtApproval
	 * @return
	 */
	Boolean saveApproval(SmtApproval smtApproval);

	/**
	 * 跟进园区id与事件类型获得审批
	 * @param parkId
	 * @param eventCode
	 * @return
	 */
	SmtApproval getApproval(Integer parkId, Integer eventCode);

}
