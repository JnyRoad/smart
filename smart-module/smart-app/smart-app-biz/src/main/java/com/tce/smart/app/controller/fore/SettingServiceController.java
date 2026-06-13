package com.tce.smart.app.controller.fore;

import com.tce.smart.app.service.fore.SettingService;
import com.tce.smart.app.vo.fore.CheckVersionVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * App设置模块控制器
 *
 * @author mkwu
 * @date 2019-07-03
 */
@RestController
@RequestMapping("/setting")
public class SettingServiceController extends BaseController {

	@Autowired
	private SettingService settingService;

	/**
	 * 检查App版本
	 *
	 * @return Result<CheckVersoinVo>
	 */
	@GetMapping("/version/check")
	public Result<CheckVersionVo> checkVersion(@RequestParam(value = "appId",required = false) String appId, @RequestParam(value = "version") String version) {
		return new Result<>(settingService.checkVersion(appId,version));
	}

	/**
	 * 验证原有手机号码
	 * @param mobile
	 * @param smsCode
	 * @return
	 */
	@GetMapping("/updatephone/verify/oldmobile/smscode")
	public Result<Boolean> checkOldMobile(@RequestParam(value = "mobile", required = true) String mobile,
										  @RequestParam(value = "smsCode", required = true) String smsCode){

		return success(settingService.verifyOldMobile(mobile,smsCode));
	}

	/**
	 * 新手机号发送手机验证码
	 * @param mobile
	 * @return
	 */
	@GetMapping("/updatephone/send/smscode")
	public Result<Boolean> sendSmsCode(@RequestParam(value = "mobile", required = true) String mobile){
		return success(settingService.sendMobileMsg(mobile));
	}
	/***
	 * 验证验证码并保存新的手机号码
	 * @param mobile
	 * @param smsCode
	 * @return
	 */
	@GetMapping("/updatephone/update")
	public Result<Boolean> updateNewMobile(@RequestParam(value = "mobile", required = true) String mobile,
									 @RequestParam(value = "smsCode", required = true) String smsCode){
		return success(settingService.updateNewPhone(mobile,smsCode));
	}


}
