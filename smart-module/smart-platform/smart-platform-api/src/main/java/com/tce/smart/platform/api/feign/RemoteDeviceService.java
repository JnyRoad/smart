package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtDeviceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 设备管理
 * @author Lenovo
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteDeviceService {

	/**
	 * 更新设备状态信息表
	 * @param entity 设备信息
	 * @return Result
	 */
	@PostMapping("/device/update/status")
	Result updateDeviceStatus(@RequestBody SmtDeviceDTO entity, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 *发起设备状态查询
	 * @return Result
	 */
	@GetMapping("/device/query/deviceStatus")
	Result queryDeviceStatus(@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

}
