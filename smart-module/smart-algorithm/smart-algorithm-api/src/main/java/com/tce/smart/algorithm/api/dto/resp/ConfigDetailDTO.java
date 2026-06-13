package com.tce.smart.algorithm.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: ConfigDetailDTO
 * @Package com.tce.smart.algorithm.api.dto.resp
 * @Description:
 * @Author wuxinjian
 * @Date 2020/2/6 16:13
 * @Version V1.0
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class ConfigDetailDTO extends BaseDTO {

	@ApiModelProperty("配置key")
	private String key;

	@ApiModelProperty("配置value")
	private String value;
}
