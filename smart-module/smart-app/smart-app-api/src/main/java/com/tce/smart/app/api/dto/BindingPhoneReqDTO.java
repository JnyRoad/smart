package com.tce.smart.app.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 修改微信绑定的手机号DTO
 * @date: 2020-08-07 9:11
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BindingPhoneReqDTO implements Serializable {
	private static final long serialVersionUID = -8415468730356619384L;

	/**
	 * 微信code
	 */
	@ApiModelProperty(value = "微信code",required = true)
	private String code;

	/**
	 * 访问者新手机号
	 */
	@ApiModelProperty(value = "新手机号",required = true)
	private String newPhone;

	/**
	 * 短信验证码
	 */
	@ApiModelProperty(value = "短信验证码",required = false)
	private String verifCode;
}
