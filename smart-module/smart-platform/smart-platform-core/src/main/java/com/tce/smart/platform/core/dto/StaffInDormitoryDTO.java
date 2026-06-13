package com.tce.smart.platform.core.dto;


import cn.hutool.core.date.DateTime;
import lombok.Data;

import java.util.List;

/**
 * 查询内宿员工
 *
 * @author 齐佩
 */
@Data
public class StaffInDormitoryDTO {


	/**
	 * 员工名称
	 */
	private String staffName;
	/**
	 * 员工工号
	 */
	private String staffBadge;
	/**
	 * 园区id
	 */
	private Integer parkId;

	/**
	 * 性别 0-男 1-女
	 */
	private Integer sex;

	/**
	 * 宿舍楼id
	 */
	private Integer dormitoryId;
	/**
	 * 宿舍楼名称
	 */
	private String dormitoryName;
	/**
	 * 楼层id
	 */
	private Integer floorId;

	/**
	 * 房间id
	 */
	private Integer roomId;

	/**
	 * 部门id
	 */
	private String depId;

	/**
	 * 职层id
	 */
	private String jcheId;


	/**
	 * 宿舍类型id
	 */
	private Integer dormitoryTypeId;


	/**
	 * 入住开始时间
	 */
	private String startTime;

	/**
	 * 入住结束时间
	 */
	private String endTime;

	/**
	 * BU
	 */
	private String compName;

	/**
	 * 是否报到
	 */
	private Integer isRegister;

	private List<Integer> dormitoryIds;
}
