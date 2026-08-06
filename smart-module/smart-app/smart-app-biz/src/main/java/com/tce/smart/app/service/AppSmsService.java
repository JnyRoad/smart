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

	String sendAndGetSmsCode(String mobile);
}
