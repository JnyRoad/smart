package com.tce.smart.platform.api.dto.req.admittance;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 访客自助查询入场申请请求。
 */
@Data
public class VisitorSelfQueryReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("申请时填写的手机号")
	private String mobile;

	@ApiModelProperty("短信验证码")
	private String smsCode;
}
