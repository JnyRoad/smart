package com.tce.smart.platform.api.dto.resp.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Li.JiaJun
 * @since 2022/4/21 9:18
 */
@Data
public class MeterImportFlushDTO extends BaseDTO {

	@ApiModelProperty("导入总数")
	private Integer maxSize;

	@ApiModelProperty("导入剩余数量")
	private Integer remainSize;
}
