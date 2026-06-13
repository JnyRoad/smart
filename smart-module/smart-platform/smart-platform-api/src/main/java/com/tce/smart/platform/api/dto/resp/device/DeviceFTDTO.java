package com.tce.smart.platform.api.dto.resp.device;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 设备体温检测设置查询DTO
 * @date: 2021/01/18
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeviceFTDTO implements Serializable {
	private static final long serialVersionUID = -1556703593312408716L;

	@ApiModelProperty(value = "园区Id")
	private Integer parkId;

	@ApiModelProperty(value = "园区名称")
	private String parkName;

	@ApiModelProperty(value = "设备Id")
	private String deviceId ;

	@ApiModelProperty(value = "设备名称")
	private String deviceName;

	@ApiModelProperty(value = "体温检测 0-不开启 1-开启")
	private Integer thermalEnable;

	@ApiModelProperty(value = "阈值")
	private String thermalThreshold;
}
