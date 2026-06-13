package com.tce.smart.platform.core.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 工资签单
 *
 * @author 王艳勇
 * @date 2019-04-13 18:19:30
 */
@Data
public class WageSignDTO implements Serializable {

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
	 * 工资月份
	 */
	private String wageDate;

	/**
	 * 签单开始时间
	 */
	private String startTime;

	/**
	 * 签单结束时间
	 */
	private String endTime;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 园区ID
	 */
	private List<Integer> parkIds;

	/**
	 * 工资签收状态
	 */
	private Integer signStatus;


}
