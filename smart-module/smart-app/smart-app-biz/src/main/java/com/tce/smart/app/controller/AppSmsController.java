package com.tce.smart.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.app.service.AppSmsService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

/**
 * App版本控制
 *
 * @author mingkai.wu
 * @date 2019-04-25 11:31:36
 */
@RestController
@RequestMapping("/sms")
public class AppSmsController extends BaseController {

	@Autowired
	private AppSmsService smsService;

	/**
	 * 发送短信验证码
	 *
	 * @param mobile 手机号
	 * @return
	 */
	@GetMapping("/send/{mobile}")
	public Result sendSmsCode(@PathVariable String mobile) {
		return success(smsService.sendSmsCode(mobile));
	}

	/**
	 * 校验短信验证码
	 *
	 * @param mobile  手机号
	 * @param smsCode 短信验证码
	 * @return
	 */
	@GetMapping("/verify")
	public Result<Boolean> verifySmsCode(@RequestParam(value = "mobile", required = true) String mobile,
			@RequestParam(value = "smsCode", required = true) String smsCode) {
		return success(smsService.verifySmsCode(mobile, smsCode));
	}


	/**
	 * 发送短信验证码,并返回验证码
	 *
	 * @param mobile 手机号
	 * @return
	 */
	@GetMapping("/send/getCode/{mobile}")
	public Result<String> sendAndGetSmsCode(@PathVariable String mobile) {
		return success(smsService.sendAndGetSmsCode(mobile));
	}

}
