package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class InDormitoryReqDTO implements Serializable {

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
