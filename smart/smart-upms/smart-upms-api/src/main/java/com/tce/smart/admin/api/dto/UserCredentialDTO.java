package com.tce.smart.admin.api.dto;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 内部工号密码认证请求。
 */
public class UserCredentialDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotBlank(message = "用户名不能为空")
	private String username;

	@NotBlank(message = "密码不能为空")
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "UserCredentialDTO{" +
				"username='" + username + '\'' +
				", password='[PROTECTED]'" +
				'}';
	}
}
