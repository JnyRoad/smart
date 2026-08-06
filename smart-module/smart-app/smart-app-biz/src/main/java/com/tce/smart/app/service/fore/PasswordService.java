package com.tce.smart.app.service.fore;

import com.tce.smart.app.vo.fore.ChackFacePwdVo;

/**
 * 密码服务接口
 *
 * @author mckaywu
 * @date 2019-06-15 16:11:47
 */
public interface PasswordService {

	/**
	 * 获取员工手机号(隐藏处理)
	 *
	 * @param badge 员工工号
	 * @return 手机号码（已做隐藏处理）
	 */
	String queryMobile(String badge);

	/**
	 * 发送短信验证码
	 *
	 * @param badge  员工工号
	 * @param mobile 手机号码
	 * @return true-成功
	 */
	Boolean sendSmsCode(String badge, String mobile);

	/**
	 * 校验短信验证码
	 *
	 * @param badge   员工号
	 * @param mobile  手机号
	 * @param smsCode 短信验证码
	 * @return 校验成功授权码
	 */
	String verifySmsCode(String badge, String mobile, String smsCode);

	/**
	 * 人脸识别校验
	 *
	 * @param facePhoto 人脸base64字符串
	 * @param deviceNo 设备编号
	 * @return 校验成功授权码
	 */
	ChackFacePwdVo verifyFace(String facePhoto,String deviceNo);

}
