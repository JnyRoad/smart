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
 * @date 2022-06-21 11:01:40
 */
@Data
public class SettlementTemplateRuleRespDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty("主键ID")
    private Long id;

	@ApiModelProperty("收费项目ID")
    private Integer categoryId;

	@ApiModelProperty("月份")
    private Integer monthNum;

	@ApiModelProperty("标准用量")
    private Double standardQty;

}
