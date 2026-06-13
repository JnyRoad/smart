package com.tce.smart.platform.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;

/**
 * 获取车位信息
 * @author Lenovo
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteParkingCountService {

	/**
	 * 获取车位信息
	 * @param parkId 园区ID
	 * @return Result
	 */
	 @GetMapping("/parking/count/{parkId}")
	 Result getByparkId(@PathVariable("parkId") Integer parkId,@RequestHeader(SecurityConstants.FROM) String from);

}
