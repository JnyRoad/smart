package com.tce.smart.platform.api.dto.req.leavecount;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:40
 */
@Data
public class SettlementTemplateEditReqDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty("模板ID")
	@NotNull(message = "模板ID不能为空")
	private Long tempId;

	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty("水电扣费项目ID：编辑时存在，新增时为空")
    private Long itemId;

	@ApiModelProperty("水电规则")
	private SettlementTemplateItemReqDTO rule;
}
