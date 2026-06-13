package com.tce.smart.platform.core.dto;

import lombok.Data;

/**
 * 床位监控
 * @author dell
 *
 */
@Data
public class DormtoryBedStatisticsDTO {


	/**
	 * 园区id
	 */
	private String parkId;


	/**
	 * 统计类型  0-按楼层  1-按性别
	 */
	private Integer statisticsType;


	private Integer sex;
}
