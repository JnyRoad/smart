package com.tce.smart.platform.api.dto.resp.leavecount;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:50
 */
@Data
public class SettlementTemplateRangeRespDTO extends BaseDTO {
private static final long serialVersionUID = 1L;


	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty("模板ID")
    private Long tempId;

	@ApiModelProperty("范围类型 1 房间 2 bu")
    private Integer type;

	@ApiModelProperty("值")
    private String value;

}
