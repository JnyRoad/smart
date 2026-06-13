package com.tce.smart.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
public class DormitoryBedPageQueryDTO {


	/**
	 * 奇数是下铺，偶数是上铺
	 */
	private Integer bedNumber;
	/**
	 * 所属房间id
	 */
	private Integer roomId;

	/**
	 * 所属楼层ID
	 */
	private Integer floorId;

	/**
	 * 所属园区ID
	 */
	private Integer parkId;

	/**
	 * 所属住宿楼ID
	 */
	private Integer dormitoryId;

	/*
	 * 员工号
	 */
	private String staffBadge;

	/**
	 * 入住员工在职状态 1-在职 0-离职
	 */
	private Integer status;

	/**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 是否空床位
	 */
	private Integer bedEmpty;

	/**
	 * 房间类型
	 */
	private Integer roomType;

	private String compName;

	private String startTime;

	private String endTime;

	private String phone;

	private List<Integer> dormitoryIds;

	private String pqcompany;
}
