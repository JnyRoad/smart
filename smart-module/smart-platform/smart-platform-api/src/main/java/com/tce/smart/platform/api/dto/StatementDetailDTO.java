package com.tce.smart.platform.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @description: StatementDetailDTO
 * @date: 2020-07-15 16:11
 * @author: wuling
 * @version: 1.0
 */
@Data
public class StatementDetailDTO {

	/**
	 * 结算月份
	 */
	@JsonFormat(pattern="yyyy-MM")
	private Date statementMonth;

	@ApiModelProperty(value = "收费项目统计")
	private List<CategoryData> categoryDataList;

	@ApiModelProperty(value = "个人结算统计")
	private List<StaffStatmentData> staffStatmentDataList;

	/**
	 * 收费项目数据
	 */
	@Builder
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CategoryData{

		@ApiModelProperty(value = "收费项目Id")
		private Integer categoryId;

		@ApiModelProperty(value = "上月止度")
		private Double preMonthNum;

		@ApiModelProperty(value = "本月止度")
		private Double curMonthNum;

		@ApiModelProperty(value = "实际用量")
		private Double actualQty;

		@ApiModelProperty(value = "总配额")
		private Double totalQty;

		@ApiModelProperty(value = "超出用量")
		private Double overQty;

		@ApiModelProperty(value = "生成明细时的超出单价")
		private BigDecimal overFee;

		@ApiModelProperty(value = "抄表开始时间")
		private Date meterStartTime;

		@ApiModelProperty(value = "抄表结束时间")
		private Date meterEndTime;

		@ApiModelProperty(value = "费用小计")
		private BigDecimal totalFee;

		@ApiModelProperty(value = "抄表类型 1.房间抄表 2.公摊抄表")
		private Integer meterType;

		@ApiModelProperty(value = "分摊房间数")
		private Integer roomNum;

		@ApiModelProperty(value = "房间均摊费用")
		private BigDecimal roomAvgFee;
	}

	/**
	 * 员工结算数据
	 */
	@Builder
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StaffStatmentData{

		@ApiModelProperty(value = "房间名称")
		private Integer roomName;

		@ApiModelProperty(value = "床铺编码")
		private Integer bedNumber;

		@ApiModelProperty(value = "员工号")
		private String staffBadge;

		@ApiModelProperty(value = "员工姓名")
		private String staffName;

		@ApiModelProperty(value = "入住时间")
		private Date inTime;

		@ApiModelProperty(value = "个人结算明细")
		private List<StaffCategoryInfo> staffCategoryInfo;

		@ApiModelProperty(value = "当月个人费用")
		private BigDecimal fee;
	}

	/**
	 * 员工结算明细数据
	 */
	@Builder
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StaffCategoryInfo{

		@ApiModelProperty(value = "收费项目Id")
		private Integer categoryId;

		@ApiModelProperty(value = "统计开始时间")
		private Date statiStartTime;

		@ApiModelProperty(value = "统计结束时间")
		private Date statiEndTime;

		@ApiModelProperty(value = "入住总天数")
		private Integer inTotalDays;

		@ApiModelProperty(value = "个人修正天数")
		private Integer reviseDays;

		@ApiModelProperty(value = "个人结算天数")
		private Integer statementDays;

		@ApiModelProperty(value = "日均摊费用")
		private BigDecimal avgFee;

		@ApiModelProperty(value = "个人结算费用")
		private BigDecimal statementFee;

		@ApiModelProperty(value = "抄表类型 1.房间抄表 2.公摊抄表")
		private Integer meterType;
	}
}
