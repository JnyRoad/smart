package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MeterreadConfigRespDTO extends BaseDTO {
	private static final long serialVersionUID = 1L;

	@JsonFormat(shape=JsonFormat.Shape.STRING)
	private Long id;

	@ApiModelProperty(value = "园区ID")
	private Integer parkId;

	@ApiModelProperty(value = "结算类型")
	private Integer type;

	@ApiModelProperty(value = "结算日")
	private Integer countDate;

}
