package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 20:06
 */
@Data
public class WaterMeterConcentratorQueryDTO extends BaseDTO {

	@ApiModelProperty("园区ID")
	private Integer parkId;

	@ApiModelProperty("集中器名称")
	private String name;

	@ApiModelProperty("状态：0、未连接；1、离线；2、在线")
	private Integer status;

	@ApiModelProperty("通信地址")
	private String address;
}
