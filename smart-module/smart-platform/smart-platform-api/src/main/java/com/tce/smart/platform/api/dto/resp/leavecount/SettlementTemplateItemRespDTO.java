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
public class SettlementTemplateItemRespDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

	@ApiModelProperty("水电扣费项目ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long itemId;

	@ApiModelProperty("水电规则")
	private List<SettlementTemplateRuleRespDTO> rules;

	@ApiModelProperty("适用级层")
	private List<SettlementTemplateJcheRespDTO> jches;

}
