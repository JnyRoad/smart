package com.tce.smart.platform.api.dto.resp.watermeter;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 10:58
 */
@Data
public class WaterMeterHisRespDTO extends BaseDTO {

	@ApiModelProperty("当前读数")
	@Excel(name = "当前读数")
	private String currentReading;

	@ApiModelProperty("采集时间")
	@Excel(name = "采集时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime collectTime;

	@ApiModelProperty("是否异常：0、否；1、是")
	private Integer isError;
}
