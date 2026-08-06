package com.tce.smart.app.service.fore;

import com.tce.smart.app.vo.fore.ChackFacePwdVo;
import com.tce.smart.app.ao.fore.PasswordResetUpdateReqDTO;

/**
 * 密码服务接口
 *
 * @author mckaywu
 * @date 2019-06-15 16:11:47
 */
public interface PasswordService {

	/**
	 * 创建密码找回 challenge。无论工号是否存在均返回不透明 challenge，避免枚举员工信息。
	 *
	 * @param badge 员工工号
	 * @return 手机号码（已做隐藏处理）
	 */
	String createPasswordResetChallenge(String badge);

	/**
	 * 发送短信验证码
	 *
	 * @param challengeId 一次性 challenge
	 * @return true-成功
	 */
	Boolean sendSmsCode(String challengeId);

	/**
	 * 校验短信验证码
	 *
	 * @param challengeId 一次性 challenge
	 * @param smsCode 短信验证码
	 * @return 校验成功授权码
	 */
	String verifySmsCode(String challengeId, String smsCode);

	/**
	 * 人脸识别校验
	 *
	 * @param facePhoto 人脸base64字符串
	 * @param deviceNo 设备编号
	 * @return 校验成功授权码
	 */
	ChackFacePwdVo verifyFace(String facePhoto,String deviceNo);

	/**
	 * 通过已核验的一次性授权码完成密码重置。
	 *
	 * @param request 不包含任何 URL 查询参数的最小请求体
	 * @return 是否修改成功
	 */
	Boolean resetPassword(PasswordResetUpdateReqDTO request);

}
