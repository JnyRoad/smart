package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: DeviceStateChangeDTO
 * @date: 2020-09-21 9:36
 * @author: wuling
 * @version: 1.0
 */
@Data
public class DeviceStateChangeDTO implements Serializable {
	private static final long serialVersionUID = 7472399104842333323L;

	@ApiModelProperty(value = "设备类型")
	private Integer deviceType;

	@ApiModelProperty(value = "设备编号")
	private String deviceCode;

	@ApiModelProperty(value = "设备连接状态")
	private Integer deviceStatus;

	@ApiModelProperty(value = "启用状态")
	private Integer enableStatus;
}
