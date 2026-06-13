package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.AddSnapVehicleReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 车辆抓拍
 * @author Lenovo
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteSnapVehicleService {

	/**
	 * 添加抓拍
	 *
	 * @param addSnapVehicleReqDTO 抓拍车辆信息
	 * @param from from
	 * @return
	 */
	@PostMapping("/snap/vehicle/save")
    Result save(@RequestBody AddSnapVehicleReqDTO addSnapVehicleReqDTO, @RequestHeader(SecurityConstants.FROM) String from);
}
