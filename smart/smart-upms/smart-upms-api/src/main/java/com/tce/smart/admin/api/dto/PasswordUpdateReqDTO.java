package com.tce.smart.admin.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 密码重置的最小请求体。密码与一次性授权码不得出现在 URL、代理日志或访问日志中。
 */
@Data
public class PasswordUpdateReqDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String username;
	private String password;
	private String updateAuthCode;
}
