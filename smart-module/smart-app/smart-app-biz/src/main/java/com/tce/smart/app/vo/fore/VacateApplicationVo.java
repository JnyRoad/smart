package com.tce.smart.app.vo.fore;

import java.util.Date;

import lombok.Data;
/**
 * 請假列表
 * @author ly
 *
 */
@Data
public class VacateApplicationVo {

	/**
	 * 申请id
	 */
	private String recordId;
	/**
	 * 员工姓名
	 */
	private String recordTitle;

	/**
	 * 记录类型备注
	 */
	private String recordDesc;

	/**
	 * 申请开始时间
	 */
	private Date startDate;
	/**
	 * 申请结束时间
	 */
	private Date endDate;

	/**
	 * 申请记录时间
	 */
	private Date recordDate;
	/**
	 * 申请时长
	 */
	private String vacateCount;
	/**
	 * 申请时长
	 */
	private String unit;



}
