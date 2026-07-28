package com.tce.smart.app.ao.fore;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 密码找回完成时的公开请求体；敏感字段不得拼接到 URL。 */
@Data
public class PasswordResetUpdateReqDTO {

	@NotBlank(message = "账号不能为空")
	private String username;

	@NotBlank(message = "新密码不能为空")
	private String password;

	@NotBlank(message = "密码修改授权不能为空")
	private String updateAuthCode;
}
