package com.tce.smart.app.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 访问者头像DTO
 * @date: 2020-08-07 9:11
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VisitorImgReqDTO implements Serializable {
	private static final long serialVersionUID = -854325216154741464L;

	/**
	 * 微信code
	 */
	@ApiModelProperty(value = "微信code",required = true)
	private String code;

	/**
	 * 访问者头像
	 */
	@ApiModelProperty(value = "访问者头像-base64编码",required = true)
	private String imgs;
}
