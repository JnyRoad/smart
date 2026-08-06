package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 密码找回短信流程使用的最小员工资料。
 */
@Data
@ApiModel("内部密码找回员工响应")
public class InternalStaffPhoneRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("脱敏手机号")
	private String maskedPhone;

	@ApiModelProperty("预留手机号，仅限服务端发送短信，不得返回客户端")
	private String phone;
}
