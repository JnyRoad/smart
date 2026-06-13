package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Li.JiaJun
 * @since 2022/7/21 14:36
 */
@Data
public class OperateLogQueryDTO extends BaseDTO {

	@ApiModelProperty("操作目标id")
	@NotNull(message = "操作目标id不能为空")
	private Long targetId;

	@ApiModelProperty("功能类型：1、水电表开关操作")
	private Integer code;

	@ApiModelProperty("操作动作：")
	private Integer action;
}
