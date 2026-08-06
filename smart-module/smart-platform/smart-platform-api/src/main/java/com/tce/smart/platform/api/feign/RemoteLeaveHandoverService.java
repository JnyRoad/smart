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

	/**
	 * 获取交接的申請信息
	 * @param processId processId
	 * @return from from
	 */
    @GetMapping("/leave/handover/get/{processId}")
	Result<Map<String,Object>> getLeaveHandoverByProcessId(@PathVariable("processId") String processId,@RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 获取交接内容項
     * @param jjr
     * @param processId
     * @return from
     */
    @GetMapping("/leave/handover/get/item/{jjr}/{processId}")
    Result<Map<String,Object>> getLeaveHandoverItemByJjr(@PathVariable("jjr") String jjr,@PathVariable("processId") String processId,@RequestHeader(SecurityConstants.FROM) String from);

	/**
     * 确认工作交接
     * @param leaveHandoverDTO 确认交接信息
     * @return
     */
	@PostMapping("/leave/handover/commit")
    Result endLeaveHandover(@RequestBody LeaveHandoverReqDTO leaveHandoverDTO, @RequestHeader(SecurityConstants.FROM) String from);


	 @GetMapping("/leave/handover/end/{processId}")
	 Result closeLeaveHandover(@PathVariable("processId") String processId,@RequestHeader(SecurityConstants.FROM) String from);

}
