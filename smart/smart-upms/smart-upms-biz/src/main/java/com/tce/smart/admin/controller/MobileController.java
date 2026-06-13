package com.tce.smart.admin.controller;

import com.tce.smart.admin.service.MobileService;
import com.tce.smart.common.core.model.Result;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 手机验证码
 */
@RestController
@AllArgsConstructor
@RequestMapping("/mobile")
@Api(value = "mobile", description = "手机管理模块")
public class MobileController {
	private final MobileService mobileService;

	@GetMapping("/{mobile}")
	public Result sendSmsCode(@PathVariable String mobile) {
		return mobileService.sendSmsCode(mobile);
	}
}
