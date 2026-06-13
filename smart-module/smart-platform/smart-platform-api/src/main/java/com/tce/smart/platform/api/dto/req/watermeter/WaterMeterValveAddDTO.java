package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 8:56
 */
@Data
public class WaterMeterValveAddDTO extends BaseDTO {

	@ApiModelProperty("阀门名称")
	@NotBlank(message = "阀门名称不能为空")
	private String name;

	@ApiModelProperty("阀门序号")
	@NotNull(message = "阀门序号不能为空")
	private Integer seq;

	@ApiModelProperty("阀门集中器ID")
	@NotNull(message = "阀门集中器ID不能为空")
	private Long concentratorId;

	@ApiModelProperty("备注")
	private String remark;

	@ApiModelProperty("标签ID数组")
	private List<Long> tagIds;
}
