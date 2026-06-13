package com.tce.smart.platform.api.feign.admittance;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * OA区域类型同步
 *
 * @author fushiping
 * @date
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteOaAreaTypeSyncService {


	/**
	 * OA区域类型同步任务
	 * @param from
	 * @return
	 */
	@GetMapping("/admittance/area/type/sync/task")
	Result syncTask(@RequestHeader(SecurityConstants.FROM) String from);
}
