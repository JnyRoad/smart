package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.*;
import com.tce.smart.platform.api.dto.req.LeaveApplicationReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 离职申请
 * @author Lenovo
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteLeaveApplicationService {

	/**
	 * 发起离职申请
	 * @param leaveApplicationDTO 离职申请信息
	 * @return Result
	 */
	@PostMapping("/leave/application/save")
	Result save(@RequestBody LeaveApplicationReqDTO leaveApplicationDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
     * 获取离职类型
     * @return
     */
    @GetMapping("/leave/application/type")
    Result<List<LeaveTypeDTO>> getLeaveType(@RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 获取离职原因
     * @return
     */
    @GetMapping("/leave/application/reason")
    Result<List<LeaveReasonDTO>> getLeaveReason(@RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 获取剩余年假天数
     * @param badge 员工号
     * @return
     */
    @GetMapping("/leave/application/year/holiday/{badge}")
    Result getYearHoliday(@PathVariable("badge") String badge,@RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 获取员工离职申请信息
     * @param processId processId
     * @param from from
     * @return
     */
    @GetMapping("/leave/application/{badge}")
    Result<Map<String, Object>> getByBadge(@PathVariable("badge") String badge,@RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 获取员工离职记录
     * @param badge 员工号
     * @param leaveStatus 离职方式
     * @return
     */
    @GetMapping("/leave/application/record/page")
    Result getProcessRecord(@RequestParam("current") Long current, @RequestParam("size") Long size, @RequestParam("badge") String badge,@RequestParam("leaveStatus") Integer leaveStatus,@RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 获取员工离职记录审批流程详情
     * @param processId processId
     * @param from from
     * @return
     */
    @GetMapping("/leave/application/record/detail/{processId}")
    Result<List<ProcessRecordFlowDTO>> getLeaveApplicationRecord(@PathVariable("processId") String processId, @RequestHeader(SecurityConstants.FROM) String from);

   /**
    * 查看工作交接
    * @param leaveApplication
    * @return
    */
    @PostMapping("/leave/handover/detail")
    Result<List<LeaveHandoverDepJjrDTO>> getLeaveHandover(@RequestBody SmtLeaveApplicationDTO leaveApplication, @RequestHeader(SecurityConstants.FROM) String from);

    // 同步OA流程方法
    @GetMapping("/leave/application/sysn/record")
    void sysnProcessRecord(@RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

}
