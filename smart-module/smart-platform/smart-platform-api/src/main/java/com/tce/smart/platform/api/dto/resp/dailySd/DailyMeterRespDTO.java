package com.tce.smart.platform.api.dto.resp.dailySd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author Li.JiaJun
 * @since 2022/7/8 10:35
 */
@Data
public class DailyMeterRespDTO extends BaseDTO {

	@ApiModelProperty("房间ID")
	private Integer roomId;

	@ApiModelProperty("房间名称")
	private String roomName;

	@ApiModelProperty("昨日-用电")
	private Double preEleNum;

	@ApiModelProperty("今日-用电")
	private Double curEleNum;

	@ApiModelProperty("实用-用电")
	private Double actEleNum;

	@ApiModelProperty("昨日-冷水")
	private Double preColdNum;

	@ApiModelProperty("今日-冷水")
	private Double curColdNum;

	@ApiModelProperty("昨日-热水")
	private Double preHotNum;

	@ApiModelProperty("今日-热水")
	private Double curHotNum;

	@ApiModelProperty("实用-用水")
	private Double actWaterNum;

	@ApiModelProperty(value = "抄表日期")
	@JsonFormat(pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date meterMonth;
}
