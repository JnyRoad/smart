package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author sunfujian
 * @date 2021/7/30 10:04
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceAuthPersonReqDTO extends BaseDTO {

	private String deviceId;

	@ApiModelProperty(value = "设备名称")
	private String deviceName;

	@ApiModelProperty(value = "设备类型")
	private Integer deviceType;

	@ApiModelProperty(value = "区域ID")
	private Integer areaId;

	@ApiModelProperty(value = "一级区域ID")
	private Integer parAreaId;

	@ApiModelProperty(value = "园区ID")
	private Integer parkId;

	@ApiModelProperty(value = "员工工号")
	private String badge;

	@ApiModelProperty(value = "园区ID集合")
	private List<Integer> parkIds;
}
