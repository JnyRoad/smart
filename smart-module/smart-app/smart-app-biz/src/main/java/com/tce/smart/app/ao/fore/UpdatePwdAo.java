package com.tce.smart.app.ao.fore;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 修改密码Ao
 *
 * @author mingkai.wu
 * @date 2019-05-09 15:13:41
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdatePwdAo extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -1543867238632474966L;

	/**
	 * 密码
	 */
	@NotBlank(message = "新密码不能为空")
	@NotEmpty(message = "新密码不能为空")
	private String password;

	/**
	 * 短信校验通过临时授权码
	 */
	@NotBlank(message = "短信授权码不能为空")
	@NotEmpty(message = "短信授权码不能为空")
	private String smsVerifyToken;
}
