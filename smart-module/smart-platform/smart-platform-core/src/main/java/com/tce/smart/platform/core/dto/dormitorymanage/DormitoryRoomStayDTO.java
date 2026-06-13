package com.tce.smart.platform.core.dto.dormitorymanage;

import lombok.Data;

/**
 * @description: 宿舍住宿情况DTO
 * @date: 2020-07-23 16:23
 * @author: wuling
 * @version: 1.0
 */
@Data
public class DormitoryRoomStayDTO {

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 楼栋ID
	 */
	private Integer dormitoryId;

	/**
	 * 房间ID
	 */
	private Integer roomId;

	/**
	 * 当前入住人数
	 */
	private Integer actCount;

	/**
	 * 楼层
	 */
	private String floorName;


}
