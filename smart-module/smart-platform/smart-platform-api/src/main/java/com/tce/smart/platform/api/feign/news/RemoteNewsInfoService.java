package com.tce.smart.platform.api.feign.news;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 厂牌挂失
 * @author fushiping
 * @date 2020/7/10 15:34
 **/
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteNewsInfoService {


	/**
	 * 检查资源是否到期
	 * @param from
	 * @return
	 */
	@GetMapping("/news/terminal/check")
	void check(@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
