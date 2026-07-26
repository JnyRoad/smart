package com.tce.smart.app.ao.fore;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/** 确认换绑命令；旧号授权状态保存在服务端，客户端不能伪造。 */
@Data
public class PhoneChangeConfirmReqDTO {

	@NotBlank(message = "新手机号不能为空")
	@Pattern(regexp = "^1[3-9]\\d{9}$", message = "新手机号格式错误")
	private String mobile;

	@NotBlank(message = "验证码不能为空")
	private String smsCode;
}
