package com.tce.smart.app.service.fore;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.tce.smart.app.vo.fore.LeaveHandoverVO;
import com.tce.smart.common.core.model.Result;

/**
 * 工作交接
 *
 * @author 王艳勇
 * @date 2019-05-10 16:16:08
 */
public interface LeaveHandoverService {

    /**
     * 获取交接内容
     * @param processId 流程ID
     * @return
     */
    Result<?> getLeaveHandoverByJjr(String processId);

    /**
     * 确认工作交接
     * @param leaveHandoverVO
     * @return
     */
    Result<?> endLeaveHandover(LeaveHandoverVO leaveHandoverVO);

    /**
     * 开始工作交接
     * @param processId 流程编号
     * @return
     */
    Result<?> startLeaveHandover(String processId);

	/**
     * 提交工作交接
     * @param processId 流程编号
     * @return
     */
	Result<?> commitLeaveHandover(String processId);

}
