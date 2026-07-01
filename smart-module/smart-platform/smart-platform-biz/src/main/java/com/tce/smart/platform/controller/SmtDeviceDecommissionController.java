package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.model.DeviceDecommissionPlan;
import com.tce.smart.platform.service.DeviceDecommissionService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备下线清理
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-设备下线清理")
@RequestMapping("/device")
public class SmtDeviceDecommissionController {

	private final DeviceDecommissionService deviceDecommissionService;

	/**
	 * 预览删除该设备会影响哪些权限组
	 *
	 * @param deviceId 设备ID
	 * @return Result
	 */
	@GetMapping("/{deviceId}/decommission/plan")
	public Result<DeviceDecommissionPlan> plan(@PathVariable("deviceId") String deviceId) {
		return new Result<>(deviceDecommissionService.plan(deviceId));
	}
}
