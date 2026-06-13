package com.tce.smart.app.vo.fore;

import lombok.Data;

/**
 * 员工宿舍信息
 * @author qipei
 *
 */
@Data
public class RoomDetailVo {

	/**
	 * 楼栋名称
	 */
	private String buildingName;

	/**
	 * 楼层名称
	 */
	private String floor;

	/**
	 * 房间名称
	 */
	private String room;

	/**
	 * 最大入住数
	 */
	private String maxBed;

	/**
	 * 床位名称
	 */
	private String bedName;

	/**
	 * 已入住人数
	 */
	private String occupancy;


	/**
	 * 房间类型
	 */
	private String roomType;

	/**
	 * 申请状态
	 */
	private String applyStateDes;

	/**
	 * 园区id
	 */
	private Integer parkId;

	/**
	 * 园区名
	 */
	private String parkName;



}
