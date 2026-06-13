package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @date 2018/6/22
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteLvwTravelService {

	/**
	 * info
	 *
	 * @param current
	 * @param size
	 * @param pedestrianBadge
	 * @param from
	 * @return
	 */
	@GetMapping("/travle/basicinfo")
    Result info(@RequestParam("current") final long current, @RequestParam("size") final long size, @RequestParam(
		"pedestrianBadge") final String pedestrianBadge, @RequestHeader(SecurityConstants.FROM) String from);

}
