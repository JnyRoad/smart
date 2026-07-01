package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 通关权限性质切换请求。
 *
 * @author claude
 * @date 2026-07-01
 */
@Data
public class AreaTypeSwitchReqDTO extends BaseDTO {

	@NotNull(message = "权限组ID不能为空")
	private Integer id;

	/**
	 * 目标权限性质 0-公共区域 1-保密区域
	 */
	@NotNull(message = "目标权限性质不能为空")
	private Integer areaType;
}
