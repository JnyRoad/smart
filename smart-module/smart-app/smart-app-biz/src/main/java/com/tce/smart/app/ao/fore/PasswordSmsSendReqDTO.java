package com.tce.smart.app.ao.fore;

import lombok.Data;

/**
 * 密码找回短信下发的最小请求体。
 *
 * challenge 仅用于关联服务端保存的找回状态，不承载工号或手机号。
 */
@Data
public class PasswordSmsSendReqDTO {

	/** 服务端创建的一次性密码找回 challenge。 */
	private String challengeId;
}
