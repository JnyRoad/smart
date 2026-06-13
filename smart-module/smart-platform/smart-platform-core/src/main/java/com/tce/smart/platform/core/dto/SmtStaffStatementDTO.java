package com.tce.smart.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @description: SmtStaffStatementDTO
 * @date: 2020-07-17 17:38
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SmtStaffStatementDTO {

	/**
	 * 员工水电结算表标识
	 */
	private Long id;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 园区名称
	 */
	private String parkName;

	/**
	 * BUID
	 */
	private Integer compId;

	/**
	 * BU名称
	 */
	private String compName;

	/**
	 * 部门ID
	 */
	private Integer depId;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 房间Id列表
	 */
	private List<Integer> roomIds;


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
	 * 入住宿舍的名称
	 */
	private String roomInName;

	/**
	 * 房间ID
	 */
	private Integer roomId;

	/**
	 * 最后入住的房间
	 */
	private String roomName;

	/**
	 * 楼栋名称
	 */
	private String dormitoryName;

	/**
	 * 入住时间
	 */
	private Date inTime;

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
	 * 日结算费用
	 */
	private BigDecimal dailyFee;

	/**
	 * 住过的房间数
	 */
	private Integer inRoomNum;

	/**
	 * 中心
	 */
	private String depAbbr;

	/**
	 * 退宿时间
	 */
	private Date outTime;

	/**
	 * 入职日期
	 */
	private Date createTime;

	/**
	 * 补贴状态
	 */
	private String hasAllowance;
}
