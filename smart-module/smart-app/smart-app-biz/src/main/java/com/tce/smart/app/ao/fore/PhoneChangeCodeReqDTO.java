package com.tce.smart.app.ao.fore;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 换绑手机号验证码命令；旧手机号由服务端从当前认证员工资料中解析。 */
@Data
public class PhoneChangeCodeReqDTO {

	@NotBlank(message = "验证码不能为空")
	private String smsCode;
}
