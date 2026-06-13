package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: AppealReplyDTO
 * @date: 2020-07-27 18:49
 * @author: wuling
 * @version: 1.0
 */
@Data
public class AppealReplyReqDTO implements Serializable {

	private static final long serialVersionUID = -8507216763759926751L;

	/**
	 * 记录Id
	 */
	@ApiModelProperty(value = "记录Id",required = true)
	private Long id;

	/**
	 * 回复内容
	 */
	@ApiModelProperty(value = "回复内容",required = true)
	private String replyDesc;
}
