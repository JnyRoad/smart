package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author sunfujian
 * @date 2021/7/29 13:46
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceTagSetReqDTO extends BaseDTO {

	@ApiModelProperty(value = "设备ID集合")
	@NotEmpty(message = "设备ID集合不能为空")
	private List<String> deviceIds;

	@ApiModelProperty(value = "标签ID集合")
	@NotEmpty(message = "标签ID集合不能为空")
	private List<Long> tagIds;
}
