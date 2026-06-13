package com.tce.smart.platform.core.vo;

import lombok.Data;

@Data
public class DormitoryRoomVO {


	/**
	 * 房间ID
	 */
		private Integer id;
	/**
	 * 房间名称，与楼层号拼接
	 */
	private String roomName;

	/**
	 * 房间号，按顺序排
	 */
	private Integer roomNum;
	/**
	 * 床位数,默认是10
	 */
	private Integer bedTotal;

	/**
	 * 占用床位数
	 */
	private Integer usedBed;

	/**
	 * 闲置床位数
	 */
	private Integer freeBed;
	/**
	 * 房间性别类型 默认是0，0-男，1-女
	 */
	private Integer roomSex;
	/**
	 * 默认是员工类型
	 */
	private Integer roomType;

	private String typeName;
	/**
	 * 是否为寝室，默认0， 0-是，1-否
	 */
	private Integer isDormitoryRoom;
	/**
	 * 所属楼层ID
	 */
	private Integer floorId;

	/**
	 * 所属园区ID
	 */
	private Integer parkId;

	private Integer isCount;

	private String parkName;

	/**
	 * 所属住宿楼ID
	 */
	private Integer dormitoryId;

	private String dormitoryName;

	/**
	 * 水电模板
	 */
	private Long sdTemplateId;
	private String templateName;

	/**
	 * 离职模板
	 */
	private Long leaveTempId;
	private String leaveTempName;
}
