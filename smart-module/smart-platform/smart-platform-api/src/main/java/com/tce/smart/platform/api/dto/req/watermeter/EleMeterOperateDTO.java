package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Li.JiaJun
 * @since 2022/5/11 10:41
 */
@Data
public class EleMeterOperateDTO extends BaseDTO {

	@ApiModelProperty("电表id数组")
	private List<Long> meterIds;
}
