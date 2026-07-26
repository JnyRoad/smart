package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.LeaveApplicationRecordDTO;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.vo.LeaveRecordVO;

import java.util.List;
import java.util.Set;

/**
 * 离职申请表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
public interface SmtLeaveApplicationService extends IService<SmtLeaveApplication> {


	/**
	 * 获取离职记录
	 * @param page 分页
	 * @param leaveApplicationRecordDTO 员工号
	 */
	IPage<SmtLeaveApplication> getPage(Page page, LeaveApplicationRecordDTO leaveApplicationRecordDTO);

	/**
	 * 获取离职流程信息
	 * @param badge 员工号
	 */
	SmtLeaveApplication getLeaveApplicationRecord(String badge);

	/**
	 * 获取离职申请信息
	 * @param processId 流程编号
	 */
	List<SmtProcessRecord> getLeaveApplication(String processId);

	/**
     * 获取离职记录
     * @param page 分页
     * @param bagde 员工号
     */
	IPage<LeaveRecordVO> getProcessRecord(Page page, String bagde, Integer leaveStatus);

	/** App 自助查询必须同时限定当前认证员工的园区范围。 */
	IPage<LeaveRecordVO> getProcessRecord(Page page, String bagde, Integer leaveStatus, Set<Integer> parkIds);
}
