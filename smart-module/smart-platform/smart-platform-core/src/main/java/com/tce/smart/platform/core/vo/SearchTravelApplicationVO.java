package com.tce.smart.platform.core.vo;

import java.util.Date;

import lombok.Data;

/**
 * 职工出差申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
@Data
public class SearchTravelApplicationVO{
	/**
	 *
	 */
	private String recordId;
	/**
	 * 记录备注
	 */
	private String recordTitle;
	/**
	 * 公司
	 */
	private String compName;

	/**
	 * 名称
	 */
	private String staffName;
	/**
	 * 记录备注：已申请
	 */
	private String recordDesc;

	/**
	 * 出差地点
	 */
	private String travelCity;

	/**
	 * 开始时间
	 */
	private Date startDate;
	/**
	 * 结束时间
	 */
	private Date endDate;
	/**
	 * 创建时间
	 */
	private Date recordDate;
	/**
	 * 岗位
	 */
	private String jobName;
}
