package com.tce.smart.platform.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 考勤汇总确认签单
 *
 * @author fushiping
 * @date 2019-04-13 18:19:30
 */
@Data
public class QueryAttendanceSignDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * buID
	 */
	private String compId;

	/**
	 * 部门ID
	 */
	private String depId;

	/**
	 * 员工号
	 */
	private String badge;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 考勤月份
	 */
	private String checkDate;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 园区ID
	 */
	private List<Integer> parkIds;

	/**
	 * 签收状态
	 */
	private Integer signStatus;

	private String phone;


}
