package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 通关权限性质切换时的冲突设备明细：
 * 该设备当前被"性质不同"的另一个权限组占用，无法完成切换。
 *
 * @author claude
 * @date 2026-07-01
 */
@Data
public class AreaTypeConflictDeviceVO extends BaseDTO {

	@ApiModelProperty(value = "设备ID")
	private String deviceId;

	@ApiModelProperty(value = "设备名称")
	private String deviceName;

	@ApiModelProperty(value = "占用该设备的其他权限组ID")
	private Integer conflictAuthorityId;

	@ApiModelProperty(value = "占用该设备的其他权限组名称")
	private String conflictAuthorityName;
}
