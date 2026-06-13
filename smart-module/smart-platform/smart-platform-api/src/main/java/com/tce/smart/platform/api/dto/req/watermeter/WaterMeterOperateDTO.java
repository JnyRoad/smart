package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Li.JiaJun
 * @since 2022/5/11 10:44
 */
@Data
public class WaterMeterOperateDTO extends BaseDTO {

	@ApiModelProperty("水表id数组")
	private List<Long> meterIds;
}
