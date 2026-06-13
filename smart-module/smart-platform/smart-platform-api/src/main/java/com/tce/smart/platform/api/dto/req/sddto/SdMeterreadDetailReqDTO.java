package com.tce.smart.platform.api.dto.req.sddto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: SdMeterreadDetailReqDTO
 * @date: 2020-07-13 15:58
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SdMeterreadDetailReqDTO implements Serializable {
	private static final long serialVersionUID = 2671218518863293793L;

	@ApiModelProperty(value = "抄表记录ID")
	private Long mrId;

	@ApiModelProperty(value = "房间ID")
	private Integer roomId;

	@ApiModelProperty(value = "抄表月份")
	@JsonFormat(pattern = "yyyy-MM")
	@DateTimeFormat(pattern = "yyyy-MM")
	private Date meterMonth;

	@ApiModelProperty(value = "抄表详情")
	private List<MeterReadDetail> meterReadDetailList;

	@Data
	@NoArgsConstructor
	public static class MeterReadDetail{

		@ApiModelProperty(value = "上月止度")
		private Double preMonthNum;

		@ApiModelProperty(value = "本月止度")
		private Double curMonthNum;

		@ApiModelProperty(value = "收费项目 1.热水 2.冷水 3.电")
		private Integer categoryId;
	}

}
