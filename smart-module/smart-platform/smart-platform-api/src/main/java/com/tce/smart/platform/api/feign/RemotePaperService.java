package com.tce.smart.platform.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;

@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemotePaperService {


	/**
	 * 更新调查问卷的状态
	 * @param fromIn
	 */
	@GetMapping("/paper/status/refresh")
	void refreshPaperStatus(@RequestHeader(SecurityConstants.FROM) String fromIn);

}
