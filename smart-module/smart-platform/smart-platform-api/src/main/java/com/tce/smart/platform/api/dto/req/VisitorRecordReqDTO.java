package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 查询访问者预约记录DTO
 * @date: 2020-08-07 9:11
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VisitorRecordReqDTO implements Serializable {
	private static final long serialVersionUID = 5002297691726091945L;

	/**
	 * 访问者手机号
	 */
	private String visitorPhone;

	/**
	 * 当前页
	 */
	private Integer current;

	/**
	 * 每页大小
	 */
	private Integer size;
}
