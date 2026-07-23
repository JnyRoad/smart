package com.tce.smart.app.service;


/**
 * 短信服务接口
 *
 * @author mkwu
 * @date 2019-05-20
 */
public interface AppSmsService {

	/**
	 * 发送短信验证码
	 *
	 * @param mobile  手机号
	 * @return true -成功，false-失败
	 */
	Boolean sendSmsCode(String mobile);

	/**
	 * 校验短信验证码
	 *
	 * @param mobile  手机号
	 * @param smsCode 短信验证码
	 * @return true -成功，false-失败
	 */
	Boolean verifySmsCode(String mobile, String smsCode);

	/**
	 * 发送访客自助流程短信验证码。
	 *
	 * <p>这是匿名入口专用能力，内部调用不得借此绕过场景限流。</p>
	 *
	 * @param mobile 访客手机号
	 * @return 对外统一的受理结果
	 */
	Boolean sendVisitorSmsCode(String mobile);

	/**
	 * 发送手机号登录短信验证码。
	 *
	 * <p>仅保留给认证流程；不能与访客场景共用匿名 URL。</p>
	 *
	 * @param mobile 登录手机号
	 * @return 对外统一的受理结果
	 */
	Boolean sendLoginSmsCode(String mobile);

	/**
	 * 校验访客自助流程短信验证码。
	 *
	 * @param mobile 访客手机号
	 * @param smsCode 短信验证码
	 * @return 校验是否通过
	 */
	Boolean verifyVisitorSmsCode(String mobile, String smsCode);

	String sendAndGetSmsCode(String mobile);
}
