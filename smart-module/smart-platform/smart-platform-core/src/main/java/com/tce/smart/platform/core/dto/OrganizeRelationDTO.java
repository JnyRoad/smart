package com.tce.smart.platform.core.dto;

import lombok.Data;

/**
 * @description: OrganizeRelationDTO
 * @date: 2020/12/31 0031 17:04
 * @author: wuling
 * @version: 1.0
 */
@Data
public class OrganizeRelationDTO {
	/**
	 * 记录Id
	 */
	private Long id;

	/**
	 * BU名称
	 */
	private String compName;

	/**
	 * 园区Id
	 */
	private Integer parkId;

	/**
	 * 园区名称
	 */
	private String parkName;
}
