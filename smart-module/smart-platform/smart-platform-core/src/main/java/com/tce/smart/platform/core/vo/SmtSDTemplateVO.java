package com.tce.smart.platform.core.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

/**
 * @description: SmtSDTemplateVO
 * @date: 2020-07-07 14:49
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtSDTemplateVO {

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

	/**
	 * 园区名称
	 */
	private String parkName;

	/**
	 * 级层ID
	 */
	private Integer jchenid;

	/**
	 * 级层名称
	 */
	private String jchenname;
}
