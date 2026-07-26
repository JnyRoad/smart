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

	/** App 发起离职仅允许当前已认证员工为本人提交。 */
	@PostMapping("/internal/app-leave/application")
	Result saveForActor(@RequestBody LeaveApplicationReqDTO request,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader("X-Smart-Actor-Park-Ids") String actorParkIds,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

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

	/** App 按流程号读取本人离职申请，Platform 以 actor 与记录归属二次校验。 */
	@GetMapping("/internal/app-leave/application/{processId}")
	Result getForActor(@PathVariable("processId") String processId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader("X-Smart-Actor-Park-Ids") String actorParkIds,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

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

	/** App 只能读取本人离职流程的审批记录。 */
	@GetMapping("/internal/app-leave/record/{processId}")
	Result<List<ProcessRecordFlowDTO>> getRecordForActor(@PathVariable("processId") String processId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader("X-Smart-Actor-Park-Ids") String actorParkIds,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

   /**
    * 查看工作交接
    * @param leaveApplication
    * @return
    */
	@PostMapping("/leave/handover/detail")
	Result<List<LeaveHandoverDepJjrDTO>> getLeaveHandover(@RequestBody SmtLeaveApplicationDTO leaveApplication, @RequestHeader(SecurityConstants.FROM) String from);

	/** App 仅能读取本人离职申请的工作交接摘要。 */
	@GetMapping("/internal/app-leave/handover/{processId}")
	Result<List<LeaveHandoverDepJjrDTO>> getHandoverForActor(@PathVariable("processId") String processId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader("X-Smart-Actor-Park-Ids") String actorParkIds,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

    // 同步OA流程方法
    @GetMapping("/leave/application/sysn/record")
    void sysnProcessRecord(@RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

}
