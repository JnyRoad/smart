package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class FloorVO {
	/**
	 * 楼层ID
	 */
	private Integer id;
	/**
	 * 所属住宿楼ID
	 */
	private Integer dormitoryId;

	/**
	 * 所属宿舍楼名称
	 */
	private String dormitoryName;
	/**
	 * 楼层数
	 */
	private Integer floorName;
	/**
	 * 房间数默认是0
	 */
	private Integer roomNum;
	/**
	 * 是否为宿舍楼层0-否 1-是 默认是1
	 */
	private Integer isDormitoryFloor;

	/**
	 * 所属园区ID
	 */
	private Integer parkId;

	/**
	 * 所属园区ID
	 */
	private String parkName;
}
