package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 20:06
 */
@Data
public class WaterValveConcentratorAddDTO extends BaseDTO {

	@ApiModelProperty("园区ID")
	@NotNull(message = "园区ID不能为空")
	private Integer parkId;

	@ApiModelProperty("阀门集中器名称")
	@NotBlank(message = "阀门集中器名称不能为空")
	private String name;

	@ApiModelProperty("阀门IP")
	@NotBlank(message = "阀门IP不能为空")
	private String ip;

	@ApiModelProperty("阀门端口")
	@NotBlank(message = "阀门端口不能为空")
	private String port;

	@ApiModelProperty("备注")
	private String remark;
}
