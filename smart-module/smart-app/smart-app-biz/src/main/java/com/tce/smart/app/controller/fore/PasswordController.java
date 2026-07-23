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
	 * 创建密码找回 challenge。接口不返回手机号、脱敏手机号或员工是否存在的信号。
	 *
	 * @param badge 员工号
	 * @return
	 */
	@GetMapping("/mobile/query")
	public Result<?> createChallenge(@RequestParam(value = "badge", required = true) String badge) {
		return success(passwordService.createPasswordResetChallenge(badge));
	}

	/**
	 * 发送短信验证码
	 *
	 * @param challengeId 一次性 challenge
	 * @return
	 */
	@GetMapping("/sms/send")
	public Result<?> sendSmsCode(@RequestParam(value = "challengeId", required = true) String challengeId) {
		return success(passwordService.sendSmsCode(challengeId));
	}

	/**
	 * 校验短信验证码
	 *
	 * @param challengeId 一次性 challenge
	 * @param smsCode 短信验证码
	 * @return
	 */
	@GetMapping("/verify")
	public Result<?> verifySmsCode(@RequestParam(value = "challengeId", required = true) String challengeId,
			@RequestParam(value = "smsCode", required = true) String smsCode) {
		return success(passwordService.verifySmsCode(challengeId, smsCode));
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
