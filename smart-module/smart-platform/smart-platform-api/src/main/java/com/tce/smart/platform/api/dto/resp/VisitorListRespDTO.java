package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 访问者预约列表DTO
 * @date: 2020-08-07 9:11
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VisitorListRespDTO implements Serializable {
	private static final long serialVersionUID = -971213847488495783L;

	/**
	 * 预约人
	 */
	@ApiModelProperty(value = "预约人",required = true)
	private String visitorName;

	/**
	 * 头像
	 */
	@ApiModelProperty(value = "头像",required = true)
	private String visitorImg;

	/**
	 * 预约开始时间
	 */
	@ApiModelProperty(value = "预约开始时间",required = true)
	@JsonFormat(pattern = "MM-dd HH:mm")
	private Date startTime;

	/**
	 * 预约结束时间
	 */
	@ApiModelProperty(value = "预约结束时间",required = true)
	@JsonFormat(pattern = "MM-dd HH:mm")
	private Date endTime;

	/**
	 * 当前状态
	 */
	@ApiModelProperty(value = "当前状态",required = true)
	private Integer status;

	/**
	 * 访问事由
	 */
	@ApiModelProperty(value = "访问事由",required = true)
	private String causeDes;
}
