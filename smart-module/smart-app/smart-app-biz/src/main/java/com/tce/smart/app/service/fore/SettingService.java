package com.tce.smart.app.service.fore;

import com.tce.smart.app.vo.fore.CheckVersionVo;

/**
 * App设置服务接口
 *
 * @author mkwu
 * @date 2019-07-03
 */
public interface SettingService {

	/**
	 * 检查App版本
	 *
	 *
	 * @param appId appId
	 * @param appVersion App当前版本号
	 * @return 最新版本信息
	 */
	CheckVersionVo checkVersion(String appId, String appVersion);

	/** 向当前认证员工的旧手机号发送验证码，客户端不能指定收件号码。 */
	boolean sendOldPhoneCode();

	/** 校验当前认证员工旧手机号验证码，并在服务端建立短时换绑授权。 */
	boolean verifyOldPhoneCode(String smsCode);

	/** 在旧手机号已验证的前提下，向用户指定的新手机号发送验证码。 */
	boolean sendNewPhoneCode(String mobile);

	/** 在旧手机号授权和新手机号验证码都成立时，完成换绑。 */
	boolean confirmNewPhone(String mobile, String smsCode);

}
