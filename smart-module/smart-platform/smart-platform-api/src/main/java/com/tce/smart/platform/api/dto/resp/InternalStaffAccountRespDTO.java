package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 内部账号识别响应。
 *
 * 仅供服务间按工号识别账号，具体业务所需字段必须另建用途明确的内部契约。
 */
@Data
@ApiModel("内部员工账号响应")
public class InternalStaffAccountRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("员工ID")
	private Long staffId;

	@ApiModelProperty("工号")
	private String badge;

	@ApiModelProperty("姓名")
	private String name;

	@ApiModelProperty("员工状态")
	private Integer status;
}
