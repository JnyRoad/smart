package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author sunfujian
 * @date 2021/8/30 15:06
 */
@Data
public class AuthDetailQueryDTO extends BaseDTO {
	@ApiModelProperty(value = "授权ID")
	@NotNull(message = "授权ID不能为空")
	private Integer authId;

	@ApiModelProperty(value = "授权类型")
	@NotNull(message = "授权类型不能为空")
	private Integer type;

	@ApiModelProperty(value = "工号")
	private String badges;

	@ApiModelProperty(value = "姓名")
	private String personName;

	@ApiModelProperty(value = "车牌号")
	private String vehiclePlate;
}
