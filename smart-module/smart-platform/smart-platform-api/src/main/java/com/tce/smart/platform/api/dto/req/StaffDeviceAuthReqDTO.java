package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author sunfujian
 * @date 2021/7/29 17:05
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffDeviceAuthReqDTO extends BaseDTO {

	@ApiModelProperty(value = "设备名称")
	private String deviceName;

	@ApiModelProperty(value = "设备类型")
	private Integer deviceType;

	@ApiModelProperty(value = "区域ID")
	private Integer areaId;
}
