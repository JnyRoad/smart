package com.tce.smart.app.ao.fore;

import lombok.Data;

/**
 * 匿名访客短信校验请求。手机号和验证码只能通过 JSON 请求体传输。
 */
@Data
public class VisitorSmsVerifyReqDTO {

	/** 访客手机号。 */
	private String mobile;
	/** 六位短信验证码。 */
	private String smsCode;
}
