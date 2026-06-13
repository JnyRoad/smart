package com.tce.smart.platform.api.dto.resp.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SdMeterStatisticsRespDTO extends BaseDTO {

	@ApiModelProperty("区域类型：0、宿舍；1、厂区")
	private Integer placeType;

	@ApiModelProperty("水电类型 1.热水 2.冷水 3.电")
	private Integer sdType;

	@ApiModelProperty("区域名称")
	private String areaName;

	@ApiModelProperty("设备名称")
	private String deviceName;

	@ApiModelProperty("年用量")
	private Double yearUse;

	@ApiModelProperty("1月用量")
	private Double month1Use;

	@ApiModelProperty("2月用量")
	private Double month2Use;

	@ApiModelProperty("3月用量")
	private Double month3Use;

	@ApiModelProperty("4月用量")
	private Double month4Use;

	@ApiModelProperty("5月用量")
	private Double month5Use;

	@ApiModelProperty("6月用量")
	private Double month6Use;

	@ApiModelProperty("7月用量")
	private Double month7Use;

	@ApiModelProperty("8月用量")
	private Double month8Use;

	@ApiModelProperty("9月用量")
	private Double month9Use;

	@ApiModelProperty("10月用量")
	private Double month10Use;

	@ApiModelProperty("11月用量")
	private Double month11Use;

	@ApiModelProperty("12月用量")
	private Double month12Use;
}
