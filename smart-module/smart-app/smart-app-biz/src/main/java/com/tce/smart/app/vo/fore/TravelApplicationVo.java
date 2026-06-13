package com.tce.smart.app.vo.fore;

import java.util.Date;

import lombok.Data;
/**
 * 出差列表
 * @author ly
 *
 */
@Data
public class TravelApplicationVo {

	/**
	 * 申请id
	 */
	private String recordId;
	/**
	 * 记录标题
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
	 * endDate
	 */
	private Date endDate;

	/**
	 * 申请记录时间
	 */
	private Date recordDate;
	/**
	 * 岗位
	 */
	private String jobName;
	/**
	 * 公司名称
	 */
	private String compName;
	/**
	 * 出差地点
	 */
	private String travelCity;
}
