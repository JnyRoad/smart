package com.tce.smart.app.controller;

import com.tce.smart.app.ao.fore.VisitorSmsSendReqDTO;
import com.tce.smart.app.ao.fore.VisitorSmsVerifyReqDTO;
import com.tce.smart.app.api.dto.InternalSmsVerifyReqDTO;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

	private final AppSmsService smsService;

	public AppSmsController(AppSmsService smsService) {
		this.smsService = smsService;
	}

	/**
	 * 发送短信验证码
	 *
	 * <p>仅用于访客自助场景；请求体避免手机号进入 URL 和访问日志。</p>
	 *
	 * @param request 访客手机号
	 * @return 统一受理结果
	 */
	@PostMapping("/visitor/send")
	public Result<Boolean> sendVisitorSmsCode(@RequestBody VisitorSmsSendReqDTO request) {
		return success(smsService.sendVisitorSmsCode(request == null ? null : request.getMobile()));
	}

	/**
	 * 手机号登录发送验证码。
	 *
	 * <p>该路由与访客场景分离，防止客户端任意声明短信用途；手机号始终位于 JSON 请求体。</p>
	 *
	 * @param request 登录手机号
	 * @return 统一受理结果
	 */
	@PostMapping("/login/send")
	public Result<Boolean> sendLoginSmsCode(@RequestBody VisitorSmsSendReqDTO request) {
		return success(smsService.sendLoginSmsCode(request == null ? null : request.getMobile()));
	}

	/**
	 * 校验短信验证码
	 *
	 * <p>仅用于访客自助场景；验证码不得通过查询串传输。</p>
	 *
	 * @param request 手机号与短信验证码
	 * @return 校验结果
	 */
	@PostMapping("/visitor/verify")
	public Result<Boolean> verifyVisitorSmsCode(@RequestBody VisitorSmsVerifyReqDTO request) {
		return success(smsService.verifyVisitorSmsCode(request == null ? null : request.getMobile(),
				request == null ? null : request.getSmsCode()));
	}

	/**
	 * 平台访客自助查询的服务间验证码校验入口。
	 *
	 * <p>只接受服务客户端令牌，普通用户令牌和匿名请求均拒绝；调用契约使用 JSON，
	 * 因而手机号和验证码不会进入服务间 URL 日志。</p>
	 *
	 * @param request 手机号与短信验证码
	 * @return 校验结果
	 */
	@Inner
	@OpenApi("server")
	@PostMapping("/internal/verify")
	public Result<Boolean> verifySmsCodeInternal(@RequestBody InternalSmsVerifyReqDTO request) {
		return success(smsService.verifySmsCode(request == null ? null : request.getMobile(),
				request == null ? null : request.getSmsCode()));
	}

	/**
	 * 匿名访客链路专用的服务间校验入口。
	 *
	 * <p>平台的记录查询和货车预约都必须走该接口，使其共享失败上限和一次性消费语义。</p>
	 *
	 * @param request 手机号与短信验证码
	 * @return 校验结果
	 */
	@Inner
	@OpenApi("server")
	@PostMapping("/internal/visitor/verify")
	public Result<Boolean> verifyVisitorSmsCodeInternal(@RequestBody InternalSmsVerifyReqDTO request) {
		return success(smsService.verifyVisitorSmsCode(request == null ? null : request.getMobile(),
				request == null ? null : request.getSmsCode()));
	}

}
