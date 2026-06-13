package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: SmtSdTemplatesReqDTO
 * @date: 2020-07-07 11:49
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtSdTemplatesReqDTO implements Serializable {
	private static final long serialVersionUID = -5078663374494528533L;

	/**
	 * 记录Id
	 */
	private Long Id;

	/**
	 * 目标名称
	 */
	private String templateName;

	/**
	 * 园区Id
	 */
	private Integer parkId;
}
