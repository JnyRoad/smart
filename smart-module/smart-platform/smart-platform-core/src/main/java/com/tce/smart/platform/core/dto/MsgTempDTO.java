package com.tce.smart.platform.core.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Li.JiaJun
 * @since 2022/9/26 14:34
 */
@Data
public class MsgTempDTO {

	@ApiModelProperty("主键ID")
	private Integer id;

	@ApiModelProperty("模板名称")
	@NotBlank(message = "模板名称不能为空")
	private String name;

	@ApiModelProperty("园区ID")
	private Integer parkId;

	@ApiModelProperty("模板类型：1、离线消息")
	private Integer msgType;

	@ApiModelProperty("发送消息的人员")
	private List<MsgPersonDTO> personList;
}
