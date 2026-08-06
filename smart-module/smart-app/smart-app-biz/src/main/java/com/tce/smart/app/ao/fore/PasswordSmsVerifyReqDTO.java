package com.tce.smart.app.ao.fore;

import lombok.Data;

/**
 * 密码找回短信校验的最小请求体。
 *
 * 验证码只能放在请求体中，避免进入 URL、代理访问日志或浏览器历史。
 */
@Data
public class PasswordSmsVerifyReqDTO {

	/** 服务端创建的一次性密码找回 challenge。 */
	private String challengeId;

	/** 用户输入的短信验证码。 */
	private String smsCode;
}
