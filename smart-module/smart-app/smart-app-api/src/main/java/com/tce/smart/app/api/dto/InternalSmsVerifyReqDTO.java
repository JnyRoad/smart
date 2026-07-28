package com.tce.smart.app.api.dto;

import lombok.Data;

/**
 * 服务间短信校验最小请求契约。
 *
 * <p>仅允许受服务令牌保护的内部接口使用，手机号和验证码不得放在 Feign URL 中。</p>
 */
@Data
public class InternalSmsVerifyReqDTO {

	/** 需要校验的手机号。 */
	private String mobile;
	/** 六位短信验证码。 */
	private String smsCode;
}
