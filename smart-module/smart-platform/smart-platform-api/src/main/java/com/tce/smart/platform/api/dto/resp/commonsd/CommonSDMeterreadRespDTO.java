package com.tce.smart.platform.api.dto.resp.commonsd;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: 公摊水电抄表详情DTO
 * @date: 2020/10/12 17:32
 * @author: wuling
 * @version: 1.0
 */
@Data
public class CommonSDMeterreadRespDTO implements Serializable {
	private static final long serialVersionUID = -8056231042246454124L;

	@ApiModelProperty(value = "公摊水电表记录ID")
	private Long sdId;

	@ApiModelProperty(value = "园区ID")
	private Integer parkId;

	@ApiModelProperty(value = "园区名称")
	private String parkName;

	@ApiModelProperty(value = "楼栋ID")
	private Integer dormitoryId;

	@ApiModelProperty(value = "楼栋名称")
	private String dormitoryName;

	@ApiModelProperty(value = "公摊水电表名称")
	private String sdName;

	@ApiModelProperty(value = "收费项目 1.热水 2.冷水 3.电")
	private Integer categoryId;

	@ApiModelProperty(value = "抄表月份")
	@JsonFormat(pattern = "yyyy-MM")
	@DateTimeFormat(pattern = "yyyy-MM")
	private Date meterMonth;

	@ApiModelProperty(value = "平均用量")
	private Double avgNum;

	@ApiModelProperty(value = "入住总人天")
	private Integer totalStayDays;

	@ApiModelProperty(value = "公摊水电抄表详情")
	private SDCategoryDTO sdCategory;

	@ApiModelProperty(value = "房间列表")
	private String roomList;

	@ApiModelProperty(value = "结算状态 0.未结算 1.已结算")
	private Integer statementStatus;

	/**
	 * 是否重置
	 */
	private Integer isRevise;

	/**
	 * 重置记录
	 */
	private List<ReviseInfo> reviseInfo;

	@Data
	public static class ReviseInfo{
		/**
		 * 抄表人
		 */
		private String meterUser;

		/**
		 * 收费项目
		 */
		private Integer categoryId;

		/**
		 * 抄表月份
		 */
		@JsonFormat(pattern="yyyy-MM")
		private Date meterMonth;

		/**
		 * 上月止度
		 */
		private Double preMonthNum;

		/**
		 * 上月止度修正数据
		 */
		private Double revPreMonthNum;

		/**
		 * 抄表时间
		 */
		private Date createTime;
	}
}
