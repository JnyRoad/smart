package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 11:24
 */
@Data
public class WaterMeterTagAddDTO extends BaseDTO {

	@ApiModelProperty("水表ID")
	@NotEmpty(message = "水表ID不能为空")
	private List<Long> meterIds;

	@ApiModelProperty("标签ID集合")
	private List<Long> tagIds;
}
