package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SdMeterStatisticsQueryDTO extends BaseDTO {

	@ApiModelProperty("区域类型：0、宿舍；1、厂区")
	private Integer placeType;

	@ApiModelProperty("区域ID/楼栋ID")
	private Integer areaId;

	@ApiModelProperty("水电类型 1.热水 2.冷水 3.电")
	private Integer sdType;

	@ApiModelProperty("设备记录Id")
	private Long deviceId;

	@ApiModelProperty("年份")
	private Integer year;
}
