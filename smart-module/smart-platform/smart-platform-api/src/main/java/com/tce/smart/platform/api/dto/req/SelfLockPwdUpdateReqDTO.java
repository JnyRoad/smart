package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 当前认证员工修改门锁动态码的请求。
 */
@Data
public class SelfLockPwdUpdateReqDTO {
	@NotBlank(message = "新密码不可为空")
	private String newPwd;
}
