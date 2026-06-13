package com.tce.smart.app.controller.fore;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.service.fore.LeaveApplicationService;
import com.tce.smart.app.vo.fore.LeaveApplicationVO;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import lombok.AllArgsConstructor;

/**
 * 离职模块 控制器
 * @author 王艳勇
 *
 */

@RestController
@AllArgsConstructor
public class LeaveApplicationController extends BaseController{

    private LeaveApplicationService leaveApplicationService;

    /**
     * 发起离职申请
     * @param leaveApplicationVO
     * @return
     */
    @PostMapping("/application/dimission")
    public Result<?> save(@RequestBody LeaveApplicationVO leaveApplicationVO) {
        return leaveApplicationService.save(leaveApplicationVO);
    }

    /**
     * 获取离职类型
     * @return
     */
    @GetMapping("/application/dimission/type")
    public Result<?> getLeaveType() {
        return leaveApplicationService.getLeaveType();
    }


    /**
     * 获取离职原因
     * @return
     */
    @GetMapping("/application/dimission/reason")
    public Result<?> getLeaveReason() {
        return leaveApplicationService.getLeaveReason();
    }

    /**
     * 获取剩余年假天数
     * @return
     */
    @GetMapping("/rest/balance/annual/get")
    public Result<?> getYearHoliday() {
        return leaveApplicationService.getYearHoliday();
    }

    /**
     * 获取员工离职申请信息
     * @param processId  流程编号
     * @return
     */
    @GetMapping("/process/dimission/get/{processId}")
    public Result<?> getLeaveApplication(@PathVariable(value="processId",required = true) String processId) {
        return leaveApplicationService.getLeaveApplication(processId);
    }

    /**
     * 获取员工离职记录
     * @param page 分页参数
     * @return
     */
    @GetMapping("/process/dimission/record/list")
    public Result<?> getProcessRecord(Page page,Integer dimissionApplyType) {
        return leaveApplicationService.getProcessRecord(page,dimissionApplyType);
    }

    /**
     * 获取员工离职记录详情
     * @param recordId 记录id
     * @return
     */
    @GetMapping("/process/dimission/record/detail/{recordId}")
    public Result<?> getLeaveApplicationRecord(@PathVariable(value="recordId",required = true) String recordId) {
        return leaveApplicationService.getLeaveApplicationRecord(recordId);
    }

     /**
     * 查看工作交接
     * @param processId 流程ID
     * @return
     */
    @GetMapping("/process/dimission/record/workhand/{processId}")
    public Result<?> getLeaveHandover(@PathVariable(value="processId",required = true) String processId) {
        return leaveApplicationService.getLeaveHandover(processId);
    }

}
