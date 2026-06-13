package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Li.JiaJun
 * @since 2022/5/11 13:45
 */
@Data
public class WaterMeterValveOperateDTO extends BaseDTO {

	@ApiModelProperty("水表id数组")
	private List<Long> meterIds;

	@ApiModelProperty("状态：0、关闭；1、开启")
	private Integer status;
}
