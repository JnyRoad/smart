package com.tce.smart.platform.core.vo;

import java.util.List;

import lombok.Data;

@Data
public class ParkStatisticsVO {


	/**
	 * 园区面积
	 */
	private Integer parkArea;

	/**
	 * 房间总数
	 */
	private Integer roomTotal;

	/**
	 * 床位总数
	 */
	private Integer bedTotal;

	/**
	 * 入住人员总数
	 */
	private Integer bedStaffTotal;

	/**
	 * 餐厅楼个数
	 */
	private Integer diningRoomTotal;

	/**
	 * 厂房总数
	 */
	private Integer workshopToal;

	/**
	 * 住宿楼总数
	 */
	private Integer dormitoryToTal;

	/**
	 * bu总数
	 */
	private Integer compTotal;

	/**
	 * 所有bu的部门总数
	 */
	private Integer deptToal;

	/**
	 * 所有bu的岗位总数
	 */
	private Integer jobTotal;

	/**
	 * 所有bu的员工总数
	 */
	private  Integer staffTotal;

	/**
	 * 每个bu的员工数
	 */
	private  List<CompStatisticsVO> compStatistics;


}
