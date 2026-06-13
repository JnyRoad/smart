package com.tce.smart.platform.core.dto;

import lombok.Data;

@Data
public class InDormitoryDTO {

	private Integer parkId;

	private String staffBadge;

	//1-上铺   2-下铺
	private Integer bedType ;

	/**
	 * 性别
	 */
	private Integer sex;

	/**
	 * 房间类型
	 */
	private Integer roomType;

}
