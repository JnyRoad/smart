package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.DeviceTaskReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.validation.Valid;

/**
 * 设备任务
 * @author 王艳用
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteDeviceTaskService {

	@PostMapping("/device/task/update")
	Result<Boolean> updateStatus(@Valid @RequestBody DeviceTaskReqDTO deviceTaskDTO, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/device/task/repeat")
	void repeat(@RequestHeader(SecurityConstants.FROM) String from);

}
