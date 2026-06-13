package com.tce.smart.platform.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 水电结算收费详情实体类
 * @date: 2020-07-24 18:11
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SDStatementFeeDetailDTO {

	/**
	 * 员工编号
	 */
	private String staffBadge;

	/**
	 * 抄表月份
	 */
	private Date meterMonth;

	/**
	 * 收费项目
	 */
	private Integer cateId;

	/**
	 * 费用
	 */
	private BigDecimal fee;

	/**
	 * 抄表类型
	 */
	private Integer meterType;

	/**
	 * 结算时间
	 */
	private Date statementDate;
}
