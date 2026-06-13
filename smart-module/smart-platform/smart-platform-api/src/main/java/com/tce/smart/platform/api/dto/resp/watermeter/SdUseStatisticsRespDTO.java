package com.tce.smart.platform.api.dto.resp.watermeter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SdUseStatisticsRespDTO extends BaseDTO {

	@ApiModelProperty("抄表ID")
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	private Long id;

	@ApiModelProperty("区域类型：0、宿舍；1、厂区")
	private Integer placeType;

	@ApiModelProperty("水电类型 1.热水 2.冷水 3.电")
	private Integer sdType;

	@ApiModelProperty("区域名称")
	private String areaName;

	@ApiModelProperty("设备名称")
	private String deviceName;

	@ApiModelProperty("设备标签")
	private String deviceTag;

	@ApiModelProperty("通讯地址")
	private String commAddress;

	@ApiModelProperty("集中器名称")
	private String concentratorName;

	@ApiModelProperty("查询开始时间")
	private String startDate;

	@ApiModelProperty("查询结束时间")
	private String endDate;

	@ApiModelProperty("查询段起数")
	private String startNum;

	@ApiModelProperty("查询段止数")
	private String endNum;

	@ApiModelProperty("查询段累计用量")
	private String sumNum;
}
