package com.tce.smart.platform.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;

@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteApplicationTaskService {


	/**
	 * 应聘者任务
	 * @return
	 */
	@GetMapping("/applicationTask/remind")
    Result applicationRemind(@RequestHeader(SecurityConstants.FROM) String from);
}
