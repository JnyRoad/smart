package com.tce.smart.platform.core.dto.dormitorymanage;

import lombok.Data;

/**
 * @description: 楼层-房间对应列表DTO
 * @date: 2020-07-23 16:23
 * @author: wuling
 * @version: 1.0
 */
@Data
public class FloorRoomListDTO {

	/**
	 * 楼层ID
	 */
	private Integer floorId;

	/**
	 * 楼层名称
	 */
	private String floorName;

	/**
	 * 房间ID
	 */
	private Integer roomId;

	/**
	 * 房间名称
	 */
	private Integer roomName;

}
