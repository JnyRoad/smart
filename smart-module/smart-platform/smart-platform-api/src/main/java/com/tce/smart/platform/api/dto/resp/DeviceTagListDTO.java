package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author sunfujian
 * @date 2021/7/29 11:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceTagListDTO extends BaseDTO {

	@ApiModelProperty(value = "主键ID")
	private Long id;

	@ApiModelProperty(value = "标签名称")
	private String tagName;
}
