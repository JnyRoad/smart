package com.tce.smart.app.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 微信公众号查询访问者预约记录DTO
 * @date: 2020-08-07 9:11
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WechatVisitorRecordReqDTO implements Serializable {
	private static final long serialVersionUID = 5002297691726091945L;

	/**
	 * 微信code
	 */
	@ApiModelProperty(value = "微信code",required = true)
	private String code;

	/**
	 * 当前页
	 */
	@ApiModelProperty(value = "当前页",required = true)
	private Integer current;

	/**
	 * 每页大小
	 */
	@ApiModelProperty(value = "每页大小",required = true)
	private Integer size;
}
