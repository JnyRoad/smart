package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: ReplyRepairReqDTO
 * @date: 2020-07-24 8:43
 * @author: wuling
 * @version: 1.0
 */
@Data
public class ReplyRepairReqDTO implements Serializable {
	private static final long serialVersionUID = 4148213969854084534L;

	/**
	 * 报修标识Id
	 */
	@ApiModelProperty("标识Id")
	private Long id;

	/**
	 * 回复状态
	 */
	@ApiModelProperty("回复状态 1.待确认 2.待维修 3.维修成功 4.已关闭")
	private Integer status;

	/**
	 * 回复内容
	 */
	@ApiModelProperty("回复内容")
	private String result;
}
