package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class SdUseStatisticsQueryDTO extends BaseDTO {

	@ApiModelProperty("区域ID/楼栋ID")
	private Integer areaId;

	@ApiModelProperty("水电类型 1.热水 2.冷水 3.电")
	private Integer sdType;

	@ApiModelProperty("开始日期")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@ApiModelProperty("结束日期")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;
}
