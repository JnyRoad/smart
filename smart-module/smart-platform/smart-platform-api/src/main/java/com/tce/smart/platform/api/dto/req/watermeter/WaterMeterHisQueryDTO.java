package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 10:43
 */
@Data
public class WaterMeterHisQueryDTO extends BaseDTO {

	@ApiModelProperty("水表ID")
	@NotNull(message = "水表ID不能为空")
	private Long waterMeterId;

	@ApiModelProperty("采集时间(开始)：格式化为：yyyy-MM-dd 00:00:00")
	private String startTime;

	@ApiModelProperty("采集时间(结束)：格式化为：yyyy-MM-dd 23:59:59")
	private String endTime;

	@ApiModelProperty("是否异常：0、否；1、是")
	private Integer isError;
}
