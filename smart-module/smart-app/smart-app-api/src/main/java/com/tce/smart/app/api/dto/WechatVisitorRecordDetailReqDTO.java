package com.tce.smart.app.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 微信公众号查询访问者预约记录详情DTO
 * @date: 2020-08-07 9:11
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WechatVisitorRecordDetailReqDTO implements Serializable {
	private static final long serialVersionUID = 5002297691726091945L;

	/**
	 * 微信code
	 */
	@ApiModelProperty(value = "微信code",required = true)
	private String code;

	/**
	 * 记录Id
	 */
	@ApiModelProperty(value = "记录Id",required = true)
	private Long id;

	/**
	 * 开始时间
	 */
	@ApiModelProperty(value = "开始时间",required = true)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date startTime;

	/**
	 * 结束时间
	 */
	@ApiModelProperty(value = "结束时间",required = true)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date endTime;
}
