package com.tce.smart.platform.api.dto.resp.leavecount;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:40
 */
@Data
public class SettlementTemplateRespDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty("模板ID")
    private Long id;

	@ApiModelProperty("模板名")
	private String templateName;

}
