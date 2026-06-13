package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.LogisticsAppointmentReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 物流车抓拍
 * @author Lenovo
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteLogisticsAppointmentService {

	/**
	 * 添加物流车抓拍信息
	 * @param logisticsAppointmentDTO 物流车抓拍信息
	 * @return
	 */
	@PostMapping("/logistics/appointment/save")
	Result save(@RequestBody LogisticsAppointmentReqDTO logisticsAppointmentDTO, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 更新超时状态
	 * @return 返回结果
	 */
	@GetMapping("/logistics/appointment/update/status")
	Result updateStatus(@RequestHeader(SecurityConstants.FROM) String from);
}
