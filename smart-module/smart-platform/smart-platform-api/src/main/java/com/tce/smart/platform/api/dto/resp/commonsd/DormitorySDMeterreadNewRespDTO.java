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
public class DormitorySDMeterreadNewRespDTO implements Serializable {
	private static final long serialVersionUID = -4418111086965329031L;

	@ApiModelProperty(value = "抄表记录Id")
	private Long mrId;

	@ApiModelProperty(value = "楼栋ID")
	private Integer dormitoryId;

	@ApiModelProperty(value = "楼栋名称")
	private String dormitoryName;

	@ApiModelProperty(value = "楼层ID")
	private Integer floorId;

	@ApiModelProperty(value = "房间ID")
	private Integer roomId;

	@ApiModelProperty(value = "房间名称")
	private String roomName;

	@ApiModelProperty(value = "房间性别属性")
	private Integer roomSex;

	@ApiModelProperty(value = "抄表日期")
	@JsonFormat(pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date meterMonth;

	@ApiModelProperty(value = "是否已抄表 0.未抄表 1.已抄表")
	private Integer status;

	@ApiModelProperty(value = "是否结算 0.未结算 1.已结算")
	private Integer statementStatus;

	@ApiModelProperty(value = "冷水上月止度")
	private Double coldPreMonthNum;

	@ApiModelProperty(value = "换表后冷水上月止度")
	private Double coldChangePreMonthNum;

	@ApiModelProperty(value = "冷水本月止度")
	private Double coldCurMonthNum;

	@ApiModelProperty(value = "冷水用量")
	private Double coldUse;

	@ApiModelProperty(value = "冷水标准用量")
	private Double coldQty;

	@ApiModelProperty(value = "冷水超标准用量")
	private Double coldOverUse;

	@ApiModelProperty(value = "冷水单价")
	private Double coldOverFee;

	@ApiModelProperty(value = "热水上月止度")
	private Double hotPreMonthNum;

	@ApiModelProperty(value = "换表后热水上月止度")
	private Double hotChangePreMonthNum;

	@ApiModelProperty(value = "热水本月止度")
	private Double hotCurMonthNum;

	@ApiModelProperty(value = "热水用量")
	private Double hotUse;

	@ApiModelProperty(value = "热水标准用量")
	private Double hotQty;

	@ApiModelProperty(value = "热水单价")
	private Double hotOverFee;

	@ApiModelProperty(value = "热水超标准用量")
	private Double hotOverUse;

	@ApiModelProperty(value = "电上月止度")
	private Double elePreMonthNum;

	@ApiModelProperty(value = "换表后电上月止度")
	private Double eleChangePreMonthNum;

	@ApiModelProperty(value = "电本月止度")
	private Double eleCurMonthNum;

	@ApiModelProperty(value = "电用量/实用")
	private Double eleUse;

	@ApiModelProperty(value = "电标准用量")
	private Double eleQty;

	@ApiModelProperty(value = "电超标准用量")
	private Double eleOverUse;

	@ApiModelProperty(value = "电单价")
	private Double eleOverFee;

	@ApiModelProperty(value = "总金额")
	private Double totalAmount;

	@ApiModelProperty(value = "入住总天数")
	private Integer inDays;

	@ApiModelProperty(value = "日均金额")
	private Double avgAmount;

	@ApiModelProperty(value = "水电模板Id")
	private Long tempId;

	@ApiModelProperty(value = "备注")
	private String remark;

	@ApiModelProperty(value = "水电表换表结算记录")
	private List<SdMeterreadDetailChangeDTO> changeList;
}
