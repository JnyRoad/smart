package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 8:56
 */
@Data
public class WaterMeterValveQueryDTO extends BaseDTO {

	@ApiModelProperty("园区Id")
	private Integer parkId;

	@ApiModelProperty("阀门名称")
	private String name;

	@ApiModelProperty("开关状态 0.关 1.开")
	private Integer openStatus;

	@ApiModelProperty("本地远程状态 0.本地 1.远程")
	private Integer remoteStatus;
}
