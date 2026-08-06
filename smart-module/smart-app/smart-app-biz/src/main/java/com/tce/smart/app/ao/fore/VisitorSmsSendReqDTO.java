package com.tce.smart.app.ao.fore;

import lombok.Data;

/**
 * 匿名访客短信发送请求。
 *
 * <p>手机号必须放在 JSON 请求体，禁止出现在 URL、浏览器历史和访问日志中。</p>
 */
@Data
public class VisitorSmsSendReqDTO {

	/** 访客手机号。 */
	private String mobile;
}
