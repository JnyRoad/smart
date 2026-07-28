package com.tce.smart.app.ao.fore;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 密码找回 challenge 的最小请求体。
 *
 * 工号不能放入 URL，避免被浏览器历史、反向代理和访问日志长期保存。
 */
@Data
public class PasswordResetChallengeReqDTO {

	/** 员工工号，只用于服务端创建一次性找回 challenge。 */
	@NotBlank(message = "工号不能为空")
	@Size(max = 64, message = "工号长度不合法")
	private String badge;
}
