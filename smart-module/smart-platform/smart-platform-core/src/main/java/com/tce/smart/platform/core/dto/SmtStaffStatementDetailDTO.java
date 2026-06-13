package com.tce.smart.platform.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: SmtStaffStatementDTO
 * @date: 2020-07-17 17:38
 * @author: fushiping
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SmtStaffStatementDetailDTO {


	/**
	 * 园区名称
	 */
	private String parkName;


	/**
	 * BU名称
	 */
	private String compName;


	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 房间
	 */
	private String roomName;

	/**
	 * 费用承担BU
	 */
	private String bearBu;

	/**
	 * 用水金额
	 */
	private BigDecimal water;

	/**
	 * 用电金额
	 */
	private BigDecimal electric;

	/**
	 * 员工工号
	 */
	private String badge;

	/**
	 * 员工姓名
	 */
	private String name;
	/**
	 * 抄表月份
	 */
	private Date meterMonth;

	/**
	 * 个人费用
	 */
	private BigDecimal fee;

	/**
	 * 结算时间
	 */
	private Date statementDate;

	/**
	 * 员工状态
	 */
	private Integer status;

	/**
	 * 楼栋名称
	 */
	private String dormitoryName;

	/**
	 * 入住时间
	 */
	private Date inTime;

	/**
	 * 索引
	 */
	private String index;

	/**
	 * 实际入住天数
	 */
	private Integer inDays;

	/**
	 * 备注天数
	 */
	private Integer remarkDays;

	/**
	 * 总入住天数
	 */
	private Integer countDays;

	/**
	 * 日均费用
	 */
	private BigDecimal avgFee;

	/**
	 * 房间ID
	 */
	private Integer roomId;

	/**
	 * 个人归属电费
	 */
	private BigDecimal realElectric;

	/**
	 * 个人归属水费
	 */
	private BigDecimal realWater;

}
