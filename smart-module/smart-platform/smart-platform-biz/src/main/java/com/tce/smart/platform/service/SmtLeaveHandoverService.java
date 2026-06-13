package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.LeaveHandoverDTO;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.entity.SmtLeaveHandover;

import java.util.List;

/**
 * 工作交接
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
public interface SmtLeaveHandoverService extends IService<SmtLeaveHandover> {

	/**
	 * 初始化交接信息
	 * @param leaveApplication 离职信息
	 * @return
	 */
	Result initLeaveHandover(SmtLeaveApplication leaveApplication);

	/**
	 * 开始工作交接
	 * @param processId 流程编号
	 * @return
	 */
	boolean startLeaveHandover(String processId);

	/**
     * 获取交接信息
     * @param processId 流程编号
     * @return
     */
	SmtLeaveApplication getLeaveHandoverByProcessId(String processId);

	/**
	 * 交接确认接口
	 * @param leaveHandoverDTO 交接确认实体
	 * @return
	 */
	boolean endLeaveHandover(LeaveHandoverDTO leaveHandoverDTO);

	/**
	 * 查看工作交接
	 * @param processId 流程编号
	 * @return
	 */
	List<SmtLeaveHandover> getLeaveHandover(String processId);

	/**
	 * 获取交接项
	 * @param processId 流程编号
	 * @param jjr 交接人工号
	 * @return
	 */
	List<SmtLeaveHandover> getLeaveHandover(String processId,String jjr);

	/**
	 * 提交离职交接信息
	 * @param leaveHandoverDTO
	 * @return
	 */
	boolean closeLeaveHandover(String processId);

}
