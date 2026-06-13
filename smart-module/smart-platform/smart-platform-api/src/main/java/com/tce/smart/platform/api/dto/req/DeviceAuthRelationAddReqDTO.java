package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author sunfujian
 * @since 2021/9/2 10:13
 */
@Data
public class DeviceAuthRelationAddReqDTO extends BaseDTO {
	@NotNull(message = "授权ID不能为空")
	private Integer authId;

	@NotNull(message = "授权类型不能为空")
	private Integer type;

	@NotEmpty(message = "工号列表不能为空")
	private List<String> badges;
}
