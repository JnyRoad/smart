package com.tce.smart.platform.service;

import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.LeaveHandoverSubmitReqDTO;
import com.tce.smart.platform.api.dto.resp.WorkDetailDTO;
import com.tce.smart.platform.core.dto.LeaveApplicationDTO;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.model.YearHoliday;
import com.tce.smart.platform.core.vo.LeaveApplicationRecordDetailVO;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 离职申请表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
public interface ILeaveApplicationService {

	/**
	 * 添加离职申请
	 * @param leaveApplicationDTO 离职申请信息
	 * @return
	 */
	Result saveLeaveApplication(LeaveApplicationDTO leaveApplicationDTO);

	/**
	 * 离职类型
	 * @return
	 */
	List<SysDict> getLeaveType();
	/**
	 * 离职原因
	 * @return
	 */
	List<SysDict> getLeaveReason();

	/**
	 * 获取剩余年假天数
	 * @param badge 员工号
	 * @return
	 */
	YearHoliday getYearHoliday(String badge);

	/**
	 * 获取OA流程
	 * @param processId
	 */
	void getOAProcess(String processId);


	/**
	 * OA 审批结束修改状态
	 * @param processId
	 * @return
	 */
	boolean endLeaveApplication(String processId);

	/**
	 * 同步OA流程方法
	 */
	void sysnProcessRecord();

	/**
	 * 离职拒接
	 * @param processId
	 * @return
	 */
	boolean failLeaveApplication(String processId);

	/**
	 * 查询考勤工时明细
	 * @return
	 */
	WorkDetailDTO getWorkDetail();

	/**
	 * 开始离职工作交接
	 * @return
	 */
	Boolean setWorkConnect();

	/**
	 * 查询审批人待处理的审批项
	 * @return
	 */
	LeaveApplicationRecordDetailVO getApproveItem(Integer id);

	/**
	 * 提交离职交接项
	 * @param submitReqDTO
	 * @return
	 */
	Boolean submitItem(LeaveHandoverSubmitReqDTO submitReqDTO);

	/**
	 * 计算伙食费
	 * @return
	 */
	BigDecimal calMealFee(SmtStaff staff, Date startDate, Date endDate);

}
