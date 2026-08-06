package com.tce.smart.platform.api.dto.req.admittance;

import lombok.Data;

import java.io.Serializable;

/** 微信网页授权 code 仅在 POST body 中提交，避免出现在 URL、代理与访问日志中。 */
@Data
public class VisitorWechatCodeReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String code;
}
