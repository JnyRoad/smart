package com.tce.smart.platform.core.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Li.JiaJun
 * @since 2022/9/26 14:49
 */
@Data
public class MsgPersonDTO {

	@ApiModelProperty("工号")
	private String badge;

	@ApiModelProperty("姓名")
	private String name;
}
