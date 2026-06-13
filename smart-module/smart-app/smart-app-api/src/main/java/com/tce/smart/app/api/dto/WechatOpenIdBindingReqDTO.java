package com.tce.smart.app.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
public class WechatOpenIdBindingReqDTO implements Serializable {
	private static final long serialVersionUID = 158063488847639736L;

	/**
	 * 微信code
	 */
	@ApiModelProperty(value = "微信code")
	@NotBlank(message = "微信code不能为空")
	private String code;

	/**
	 * 工号
	 */
	@ApiModelProperty(value = "工号")
	@NotBlank(message = "工号不能为空")
	private String badge;

	/**
	 * 身份证后六位
	 */
	@ApiModelProperty(value = "身份证后六位")
	@NotBlank(message = "身份证后六位")
	private String lastCertNum;

	/**
	 * 园区ID
	 */
	@ApiModelProperty(value = "园区ID")
	@NotNull(message = "园区ID不能为空")
	private Integer parkId;
}
