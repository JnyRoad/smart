package com.tce.smart.platform.api.dto.req.sddto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 添加公共水电抄表记录DTO
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SaveCommonSDMeterreadReqDTO implements Serializable {
	private static final long serialVersionUID = -6082332517663810979L;

	@ApiModelProperty(value = "记录ID")
	private Long mrId;

	@ApiModelProperty(value = "水电表记录ID",required = true)
	private Long commonId;

	@ApiModelProperty(value = "抄表月份",required = true)
	@JsonFormat(pattern = "yyyy-MM")
	@DateTimeFormat(pattern = "yyyy-MM")
	private Date meterMonth;

	@ApiModelProperty(value = "上月止度",required = true)
	private Double preMonthNum;

	@ApiModelProperty(value = "本月止度",required = true)
	private Double curMonthNum;
}
