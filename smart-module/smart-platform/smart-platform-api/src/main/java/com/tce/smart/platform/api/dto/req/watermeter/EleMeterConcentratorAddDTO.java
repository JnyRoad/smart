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
public class EleMeterConcentratorAddDTO extends BaseDTO {

	@ApiModelProperty("园区ID")
	@NotNull(message = "园区ID不能为空")
	private Integer parkId;

	@ApiModelProperty("电表集中器名称")
	@NotBlank(message = "电表集中器名称不能为空")
	private String name;

	@ApiModelProperty("电表集中器IP")
	@NotBlank(message = "电表集中器IP不能为空")
	private String ip;

	@ApiModelProperty("电表集中器通信端口号")
	private String port;

	@ApiModelProperty("备注")
	private String remark;

	@ApiModelProperty("通信地址")
	private String address;
}
