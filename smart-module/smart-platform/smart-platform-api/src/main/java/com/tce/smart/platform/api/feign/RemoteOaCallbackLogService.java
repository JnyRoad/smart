package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * OA 回调日志维护（内部调用，smart-schedule 定时任务专用）
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE, contextId = "remoteOaCallbackLogService")
public interface RemoteOaCallbackLogService {

	/**
	 * 过期回调日志清理任务（90 天整行删除，spec 2026-07-05 §3.2）
	 * @param from 内部调用标识（SecurityConstants.FROM_IN）
	 * @return 删除行数
	 */
	@GetMapping("/oa/workflow/callback/log/clean")
	Result cleanTask(@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
