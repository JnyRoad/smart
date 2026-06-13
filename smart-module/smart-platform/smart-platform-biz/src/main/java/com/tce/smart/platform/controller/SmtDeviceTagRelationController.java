package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.DeviceTagSetReqDTO;
import com.tce.smart.platform.service.SmtDeviceTagRelationService;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sunfujian
 * @date 2021/7/29 14:13
 */
@RestController
@RequestMapping("/device/tag/")
@AllArgsConstructor
public class SmtDeviceTagRelationController extends BaseController {

	private final SmtDeviceTagRelationService deviceTagRelationService;

	@PostMapping("/set")
	@ApiOperation(value = "设置设备标签")
	public Result<Boolean> setTag(@RequestBody DeviceTagSetReqDTO tagSetReqDTO) {
		return success(deviceTagRelationService.saveBatch(tagSetReqDTO));
	}
}
