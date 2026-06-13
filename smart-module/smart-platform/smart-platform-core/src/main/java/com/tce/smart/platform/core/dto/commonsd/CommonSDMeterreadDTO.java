package com.tce.smart.platform.core.dto.commonsd;

import lombok.Data;

import java.util.Date;

/**
 * @description: 公摊水电抄表记录DTO
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class CommonSDMeterreadDTO {

	/**
	 * 公摊水电表记录ID
	 */
	private Long id;

	/**
	 * 房间名称
	 */
	private String sdName;

	/**
	 * 抄表月份
	 */
	private Date meterMonth;

	/**
	 * 结算状态
	 */
	private Integer statementStatus;

	/**
	 * 收费项目
	 */
	private Integer categoryId;


	/**
	 * 上月止度
	 */
	private Double preMonthNum;

	/**
	 * 上月止度修正数据
	 */
	private Double revPreMonthNum;

	/**
	 * 本月止度
	 */
	private Double curMonthNum;

	/**
	 * 上月止度是否修正
	 */
	private Integer isRevise;

	/**
	 * 抄表人员
	 */
	private String meterUser;

	/**
	 * 抄表时间
	 */
	private Date createTime;
}
