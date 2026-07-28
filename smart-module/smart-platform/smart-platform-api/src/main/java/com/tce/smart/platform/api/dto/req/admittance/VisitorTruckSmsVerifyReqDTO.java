package com.tce.smart.platform.api.dto.req.admittance;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 匿名货车预约的短信本人核验请求。
 */
@Data
public class VisitorTruckSmsVerifyReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("司机手机号")
	private String mobile;

	@ApiModelProperty("短信验证码")
	private String smsCode;
}
