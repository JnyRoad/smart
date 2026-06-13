package com.tce.smart.app.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 微信绑定信息DTO
 * @date: 2020-08-07 9:11
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WechatBindingInfoDTO implements Serializable {
	private static final long serialVersionUID = 3155562288495938316L;

	/**
	 * 是否绑定
	 */
	@ApiModelProperty(value = "是否绑定 true:已绑定 false:未绑定",required = true)
	private Boolean isBinding;

	/**
	 * 访问者手机号
	 */
	@ApiModelProperty(value = "访问者手机号",required = true)
	private String visitPhone;

	/**
	 * 访问者头像地址
	 */
	@ApiModelProperty(value = "访问者头像地址",required = true)
	private String visitImgUrl;
}
