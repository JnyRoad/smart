package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.*;
import com.tce.smart.platform.api.dto.resp.SmtDormitoryRepairsRespVO;
import com.tce.smart.platform.api.dto.resp.dormitoryrepairs.SmtDormitoryRepairsDetailDTO;
import com.tce.smart.platform.core.entity.ApproveList;
import com.tce.smart.platform.core.entity.SmtDormitoryRepairs;
import com.tce.smart.platform.core.vo.SmtDormitoryRepairsVO;

import java.util.List;

/**
 * @description: SmtDormitoryRepairsService
 * @date: 2020-07-20 13:58
 * @author: wuling
 * @version: 1.0
 */
public interface SmtDormitoryRepairsService extends IService<SmtDormitoryRepairs> {
	/**
	 * 分页查询宿舍报修记录
	 * @param page
	 * @param smtDormitoryRepairsReqDTO
	 * @return
	 */
	IPage<SmtDormitoryRepairsVO> getDormitoryRepairsPage(Page page, SmtDormitoryRepairsReqDTO smtDormitoryRepairsReqDTO);

	/**
	 * 分页查询宿舍报修记录
	 * @param page
	 * @param dto
	 * @return
	 */
	IPage<SmtDormitoryRepairsVO> getDormitoryRepairsPage(Page page, SmtDormitoryRepairsReqYutoDTO dto);

	/**
	 * 添加宿舍报修记录
	 * @param smtDormitoryRepairsReqDTO
	 * @return
	 */
	boolean addDormitoryRepairs(SmtDormitoryRepairsAddReqDTO smtDormitoryRepairsAddReqDTO);

	/**
	 * 分页查询员工报修记录
	 * @param page
	 * @param smtDormitoryRepairsReqDTO
	 * @return
	 */
	IPage<SmtDormitoryRepairsRespVO> getDormitoryRepairsPageByStaff(Page page);

	/**
	 * 查询员工报修详细
	 * @param id
	 * @param flag 判断是否计算审批人
	 * @return
	 */
	SmtDormitoryRepairsDetailDTO getStaffReportDetail(Long id);

	/**
	 * 园区报修审批
	 * @param id
	 * @param approveBadge
	 * @param status
	 * @param remark
	 * @return
	 */
	Boolean updateStatus(Long id, String approveBadge, Integer status, String remark);

	/**
	 * 报修回复
	 * 1. 添加回复记录
	 * 2. 修改报修状态
	 * @param replyRepairReqDTO
	 * @return
	 */
	boolean replyRepair(ReplyRepairReqDTO replyRepairReqDTO);

}
