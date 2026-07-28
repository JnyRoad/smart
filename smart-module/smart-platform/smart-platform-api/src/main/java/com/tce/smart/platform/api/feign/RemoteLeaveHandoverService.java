package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.LeaveHandoverReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 工作交接
 * @author Lenovo
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteLeaveHandoverService {


    /**
     * 开始工作交接
     * @param processId
     * @return Result
     */
	@GetMapping("/leave/handover/start/{processId}")
	Result startLeaveHandover(@PathVariable("processId") String processId,@RequestHeader(SecurityConstants.FROM) String from);

	/** 只有离职申请本人可启动自己的交接流程。 */
	@GetMapping("/internal/app-leave/handover/start/{processId}")
	Result startHandoverForActor(@PathVariable("processId") String processId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader("X-Smart-Actor-Park-Ids") String actorParkIds,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	/**
	 * 获取交接的申請信息
	 * @param processId processId
	 * @return from from
	 */
	@GetMapping("/leave/handover/get/{processId}")
	Result<Map<String,Object>> getLeaveHandoverByProcessId(@PathVariable("processId") String processId,@RequestHeader(SecurityConstants.FROM) String from);

	/** 交接人只能按自己的登录工号读取被分配的交接内容。 */
	@GetMapping("/internal/app-leave/handover/assignee/{processId}")
	Result<Map<String,Object>> getHandoverForAssignee(@PathVariable("processId") String processId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader("X-Smart-Actor-Park-Ids") String actorParkIds,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

    /**
     * 获取交接内容項
     * @param jjr
     * @param processId
     * @return from
     */
    @GetMapping("/leave/handover/get/item/{jjr}/{processId}")
	Result<Map<String,Object>> getLeaveHandoverItemByJjr(@PathVariable("jjr") String jjr,
			@PathVariable("processId") String processId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader("X-Smart-Actor-Park-Ids") String actorParkIds,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	/**
     * 确认工作交接
     * @param leaveHandoverDTO 确认交接信息
     * @return
     */
	@PostMapping("/leave/handover/commit")
	Result endLeaveHandover(@RequestBody LeaveHandoverReqDTO leaveHandoverDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/** 交接确认人由 Platform 强制设为 actor，忽略客户端伪造的 jjr。 */
	@PostMapping("/internal/app-leave/handover/commit")
	Result endHandoverForActor(@RequestBody LeaveHandoverReqDTO request,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader("X-Smart-Actor-Park-Ids") String actorParkIds,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);


	@GetMapping("/leave/handover/end/{processId}")
	Result closeLeaveHandover(@PathVariable("processId") String processId,@RequestHeader(SecurityConstants.FROM) String from);

	/** 只有离职申请本人可触发最终提交。 */
	@GetMapping("/internal/app-leave/handover/close/{processId}")
	Result closeHandoverForActor(@PathVariable("processId") String processId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader("X-Smart-Actor-Park-Ids") String actorParkIds,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

}
