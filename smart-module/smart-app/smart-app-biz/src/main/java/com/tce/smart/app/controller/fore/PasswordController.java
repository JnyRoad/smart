package com.tce.smart.app.controller.fore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.app.ao.fore.PerfectInfoAo;
import com.tce.smart.app.service.fore.PasswordService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import lombok.AllArgsConstructor;

/**
 * 密码服务控制器
 *
 * @author mingkai.wu
 * @date 2019-05-09 15:15:37
 */
@RestController
@AllArgsConstructor
@RequestMapping("/password")
public class PasswordController extends BaseController {

	@Autowired
	private PasswordService passwordService;

	/**
	 * 获取员工手机号(隐藏部分手机号数)
	 *
	 * @param badge 员工号
	 * @return
	 */
	@GetMapping("/mobile/query")
	public Result<?> sendSmsCode(@RequestParam(value = "badge", required = true) String badge) {
		return success(passwordService.queryMobile(badge));
	}

	/**
	 * 发送短信验证码
	 *
	 * @param badge  员工号
	 * @param mobile 手机号
	 * @return
	 */
	@GetMapping("/sms/send")
	public Result<?> sendSmsCode(@RequestParam(value = "badge", required = true) String badge,
			@RequestParam(value = "mobile", required = true) String mobile) {
		return success(passwordService.sendSmsCode(badge, mobile));
	}

	/**
	 * 校验短信验证码
	 *
	 * @param badge   员工号
	 * @param mobile  手机号
	 * @param smsCode 短信验证码
	 * @return
	 */
	@GetMapping("/verify")
	public Result<?> verifySmsCode(@RequestParam(value = "badge", required = true) String badge,
			@RequestParam(value = "mobile", required = true) String mobile,
			@RequestParam(value = "smsCode", required = true) String smsCode) {
		return success(passwordService.verifySmsCode(badge, mobile, smsCode));
	}

	/**
	 * 人脸识别校验
	 *
	 * @param ocrAo  人脸信息
	 * @return
	 */
	@PostMapping("/verify/face")
	public Result<?> verifyFace(@RequestBody PerfectInfoAo ocrAo) {
		return success(passwordService.verifyFace(ocrAo.getFacePhoto(),ocrAo.getDeviceNo()));
	}
}
