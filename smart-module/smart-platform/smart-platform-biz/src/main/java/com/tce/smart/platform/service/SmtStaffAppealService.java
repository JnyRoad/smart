package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.AppealReplyReqDTO;
import com.tce.smart.platform.api.dto.req.SmtSecurityAreaSupplierReqDTO;
import com.tce.smart.platform.api.dto.req.SmtStaffAppealReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtStaffAppealListVO;
import com.tce.smart.platform.api.dto.resp.SmtStaffAppealQueryVO;
import com.tce.smart.platform.core.dto.SmtSecurityAreaSupplierDTO;
import com.tce.smart.platform.core.dto.StaffAppealSearchDTO;
import com.tce.smart.platform.core.entity.SmtStaffAppeal;
import com.tce.smart.platform.core.vo.SmtStaffAppealVO;

/**
 * @description: SmtStaffAppealService
 * @date: 2020-07-23 14:05
 * @author: wuling
 * @version: 1.0
 */
public interface SmtStaffAppealService extends IService<SmtStaffAppeal> {

	/**
	 * 根据条件分页查询员工申诉记录
	 * @param page
	 * @param staffAppealSearchDTO
	 * @return
	 */
	IPage<SmtStaffAppealVO> getStaffAppealPage(Page page, StaffAppealSearchDTO staffAppealSearchDTO);


	/**
	 * 查询员工的申诉记录
	 * @param page
	 * @param staffBadge
	 * @return
	 */
	IPage<SmtStaffAppealListVO> getStaffAppealRecord(Page page);

	/**
	 * 保存员工申诉记录
	 * @param smtStaffAppealReqDTO
	 * @return
	 */
	boolean saveStaffAppealRecord(SmtStaffAppealReqDTO smtStaffAppealReqDTO);

	/**
	 * 查询申诉详细信息
	 * @param id
	 * @return
	 */
	SmtStaffAppealQueryVO getAppealDetail(Long id);

	/**
	 * 保存回复内容
	 * @param id 记录Id
	 * @param replyDesc 回复内容
	 * @return
	 */
	boolean saveReplyDesc(AppealReplyReqDTO appealReplyReqDTO);


	/**
	 * 添加待批复记录
	 * @param id
	 * @param changeBadge
	 */
	boolean AddApproveList(Long id,String changeBadge);

}
