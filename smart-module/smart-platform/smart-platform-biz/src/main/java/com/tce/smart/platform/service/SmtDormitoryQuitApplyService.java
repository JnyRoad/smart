package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.*;
import com.tce.smart.platform.core.entity.SmtDormitoryQuitApply;

/**
 * 退宿申请与审批
 *
 * @Auther: fushiping
 * @Date:
 */

public interface SmtDormitoryQuitApplyService extends IService<SmtDormitoryQuitApply> {

	/**
	 * 退宿申请分页查询
	 * @param page
	 * @param reqDTO
	 * @return
	 */
	IPage<SmtDormitoryQuitApply> getPage(Page page, DormitoryQuitApplyQueryDTO reqDTO);

	/**
	 * 查询审批列表
	 * @param
	 * @return
	 */
	IPage<SmtDormitoryQuitApply> getApprovalList(Page page,QuitDorApplyQueryReqDTO query);

	/**
	 * 每晚零点退宿
	 * @return
	 */
	Boolean dealyQuit();

	/**
	 * 查询保安确认列表
	 * @param parkId
	 * @return
	 */
	IPage<SmtDormitoryQuitApply> getCheckList(Page page, Integer parkId);

	/**
	 * 查询确认
	 * @param code
	 * @return
	 */
	SmtDormitoryQuitApply getCheckByCode(String code);

	/**
	 * 审批
	 * @param id
	 * @param approveBadge
	 * @param status
	 * @param remark
	 * @return
	 */
	Boolean status(Long id, String approveBadge, Integer status, String remark);

	/**
	 * 新增退宿申请
	 *
	 * @param reqDTO
	 * @return
	 */
	Boolean saveApply(DormitoryQuitApplyEditReqDTO reqDTO);

}
