package com.tce.smart.platform.core.dto.dormitorymanage;

import lombok.Data;

import java.util.List;

/**
 * @description: 查询宿舍详情DTO
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SearchDormitoryRoomDetailDTO {

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 楼栋ID
	 */
	private Integer dormitoryId;

	/**
	 * 楼层ID
	 */
	private Integer floorId;

	/**
	 * 房间类型
	 */
	private Integer roomType;

	/**
	 * 入住状态 1-未满， 2-已满， 3-空房
	 */
	private Integer inStatus;

	/**
	 * 房间性别类型 0-男，1-女，2-夫妻，3-其他
	 */
	private Integer sex;

	/**
	 * 是否正常房间 1-正常，2-异常 默认正常
	 */
	private Integer isNormal = 1;


	/**
	 * 楼栋ID
	 */
	private List<Integer> dormitoryIds;
}
