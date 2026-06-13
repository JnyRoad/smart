package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LockPwdUpdateDTO {

	/**
	 * 员工工号
	 */
	@NotBlank(message = "工号不可为空")
	private String badge;

	/**
	 * 新密码
	 */
	@NotBlank(message = "新密码不可为空")
	private String newPwd;

}
