package com.tce.smart.platform.api.dto.req.watermeter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 11:07
 */
@Data
public class WaterMeterValveUpdateDTO extends BaseDTO {
	@ApiModelProperty("主键ID")
	@JsonSerialize(using = ToStringSerializer.class)
	@NotNull(message = "主键ID不能为空")
	private Long id;

	@ApiModelProperty("名称")
	private String name;

	@ApiModelProperty("序号")
	private Integer seq;

	@ApiModelProperty("集中器ID")
	private Long concentratorId;

	@ApiModelProperty("备注")
	private String remark;

	@ApiModelProperty("标签ID数组")
	private List<Long> tagIds;
}
