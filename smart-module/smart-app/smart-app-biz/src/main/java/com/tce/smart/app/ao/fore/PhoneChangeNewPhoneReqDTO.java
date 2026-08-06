package com.tce.smart.app.ao.fore;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/** 换绑新手机号请求；只允许在服务端确认旧手机号后使用。 */
@Data
public class PhoneChangeNewPhoneReqDTO {

	@NotBlank(message = "新手机号不能为空")
	@Pattern(regexp = "^1[3-9]\\d{9}$", message = "新手机号格式错误")
	private String mobile;
}
