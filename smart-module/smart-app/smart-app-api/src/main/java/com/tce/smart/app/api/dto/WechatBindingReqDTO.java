package com.tce.smart.app.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 微信绑定DTO
 * @date: 2020-08-07 9:11
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WechatBindingReqDTO implements Serializable {
	private static final long serialVersionUID = 158063488847639736L;

	/**
	 * 微信code
	 */
	@ApiModelProperty(value = "微信code",required = true)
	private String code;

	/**
	 * 访问者手机号
	 */
	@ApiModelProperty(value = "访问者手机号",required = true)
	private String visitPhone;

	/**
	 * 手机验证码
	 */
	@ApiModelProperty(value = "手机验证码",required = false)
	private String verifCode;
}
