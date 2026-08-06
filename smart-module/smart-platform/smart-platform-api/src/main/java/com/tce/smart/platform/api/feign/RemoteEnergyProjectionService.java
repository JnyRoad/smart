package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/** 能耗投影的内部任务调用契约。 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteEnergyProjectionService {
	@PostMapping("/inner/energy/projection/process-pending")
	Result<Boolean> processPending(@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@PostMapping("/inner/energy/projection/reconcile/{businessDate}")
	Result<Boolean> reconcile(@PathVariable("businessDate") String businessDate,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@PostMapping("/inner/energy/projection/backfill-month-to-date")
	Result<Boolean> backfillMonthToDate(@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@PostMapping("/inner/energy/projection/daily/{businessDate}")
	Result<Boolean> daily(@PathVariable("businessDate") String businessDate, @RequestParam("reconcile") boolean reconcile,
			@RequestParam("backfill") boolean backfill, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
