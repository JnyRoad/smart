package com.tce.smart.app.service.fore;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.vo.fore.LeaveApplicationVO;
import com.tce.smart.common.core.model.Result;


/**
 * 离职申请
 *
 * @author 王艳勇
 * @date 2019-05-10 16:16:08
 */
public interface LeaveApplicationService {

    /**
     * 发起离职申请
     * @param leaveApplicationVO
     * @return
     */
    Result<?> save(LeaveApplicationVO leaveApplicationVO);

    /**
     * 获取离职类型
     * @return
     */
    Result<?> getLeaveType();

    /**
     * 获取离职原因
     * @return
     */
    Result<?> getLeaveReason();

    /**
     * 获取剩余年假天数
     * @return
     */
    Result<?> getYearHoliday();

    /**
     * 获取员工离职申请信息
     * @param processId 流程编号
     * @return
     */
    Result<?> getLeaveApplication(String processId);

    /**
     * 获取员工离职记录
     * @param page 分页参数
     * @return
     */
    Result<?> getProcessRecord(Page page,Integer dimissionApplyType);

    /**
     * 获取员工离职记录详情
     * @param recordId 记录id
     * @return
     */
    Result<?> getLeaveApplicationRecord(String recordId);

    /**
     * 查看工作交接
     * @param processId 流程ID
     * @return
     */
    Result<?> getLeaveHandover(String processId);

}
