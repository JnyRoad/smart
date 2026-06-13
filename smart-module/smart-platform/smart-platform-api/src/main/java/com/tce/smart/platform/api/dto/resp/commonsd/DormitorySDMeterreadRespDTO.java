package com.tce.smart.platform.api.dto.resp.commonsd;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: 查询宿舍水电抄表信息响应DTO
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class DormitorySDMeterreadRespDTO implements Serializable {
	private static final long serialVersionUID = -4418111086965329031L;

	@ApiModelProperty(value = "房间ID")
	private Integer roomId;

	@ApiModelProperty(value = "房间名称")
	private Integer roomName;

	@ApiModelProperty(value = "抄表月份")
	@JsonFormat(pattern = "yyyy-MM")
	@DateTimeFormat(pattern = "yyyy-MM")
	private Date meterMonth;

	@ApiModelProperty(value = "入住总天数")
	private Integer inDays;

	@ApiModelProperty(value = "是否已抄表 0.未抄表 1.已抄表")
	private Integer status;

	@ApiModelProperty(value = "是否结算 0.未结算 1.已结算")
	private Integer statementStatus;

	@ApiModelProperty(value = "宿舍抄表数据")
	private List<Cate> dormitoryCates;

	@ApiModelProperty(value = "公摊抄表数据")
	private List<CommonCate> commonCates;

	@Data
	public static class Cate{

		@ApiModelProperty(value = "收费项目 1.热水 2.冷水 3.电")
		private Integer categoryId;

		@ApiModelProperty(value = "上月止度")
		private Double preMonthNum;

		@ApiModelProperty(value = "本月止度")
		private Double curMonthNum;

		@ApiModelProperty(value = "上月止度是否修正 0.未修正 1.已修正")
		private Integer isRevise;

		@ApiModelProperty(value = "人均每天用量")
		private Double avgNum;

	}

	@Data
	public static class CommonCate{

		@ApiModelProperty(value = "收费项目 1.热水 2.冷水 3.电")
		private Integer categoryId;

		@ApiModelProperty(value = "人均每天用量")
		private Double avgNum;

	}
}
