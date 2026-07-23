package com.tce.smart.admin.api.dto;

import lombok.Data;

/**
 * 手机号登录服务间短信校验请求。
 *
 * <p>该对象仅作为 Feign JSON 请求体，禁止把手机号或验证码拼入请求 URL。</p>
 */
@Data
public class InternalSmsVerifyReqDTO {

	/** 登录手机号。 */
	private String mobile;
	/** 短信验证码。 */
	private String smsCode;
}
