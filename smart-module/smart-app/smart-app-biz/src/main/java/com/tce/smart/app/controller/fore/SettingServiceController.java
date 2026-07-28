package com.tce.smart.app.controller.fore;

import com.tce.smart.app.ao.fore.PhoneChangeCodeReqDTO;
import com.tce.smart.app.ao.fore.PhoneChangeConfirmReqDTO;
import com.tce.smart.app.ao.fore.PhoneChangeNewPhoneReqDTO;
import com.tce.smart.app.service.fore.SettingService;
import com.tce.smart.app.vo.fore.CheckVersionVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

	/** 向当前会话绑定的旧手机号发送验证码，手机号不会出现在请求体、URL 或日志中。 */
	@PostMapping("/phone/old/send")
	public Result<Boolean> sendOldPhoneCode() {
		return success(settingService.sendOldPhoneCode());
	}

	/** 校验旧手机号验证码并建立服务端短时授权，后续新号操作必须检查该授权。 */
	@PostMapping("/phone/old/verify")
	public Result<Boolean> verifyOldPhoneCode(@Valid @RequestBody PhoneChangeCodeReqDTO request) {
		return success(settingService.verifyOldPhoneCode(request.getSmsCode()));
	}

	/** 仅在旧手机号已验证的会话里，向新手机号发送验证码。 */
	@PostMapping("/phone/new/send")
	public Result<Boolean> sendNewPhoneCode(@Valid @RequestBody PhoneChangeNewPhoneReqDTO request) {
		return success(settingService.sendNewPhoneCode(request.getMobile()));
	}

	/** 双重验证后更新新手机号；成功后销毁旧手机号验证状态。 */
	@PostMapping("/phone/new/confirm")
	public Result<Boolean> confirmNewPhone(@Valid @RequestBody PhoneChangeConfirmReqDTO request) {
		return success(settingService.confirmNewPhone(request.getMobile(), request.getSmsCode()));
	}


}
