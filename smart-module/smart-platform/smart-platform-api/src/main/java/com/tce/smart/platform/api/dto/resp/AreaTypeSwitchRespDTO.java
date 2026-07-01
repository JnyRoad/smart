package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 通关权限性质切换的返回结果。
 * success=false 时表示存在跨权限组冲突设备，本次切换未写库，
 * conflicts 里是需要管理员先去对应权限组手动移除的设备清单。
 *
 * @author claude
 * @date 2026-07-01
 */
@Data
public class AreaTypeSwitchRespDTO extends BaseDTO {

	@ApiModelProperty(value = "是否切换成功")
	private boolean success;

	@ApiModelProperty(value = "冲突设备清单，仅 success=false 时有值")
	private List<AreaTypeConflictDeviceVO> conflicts;
}
