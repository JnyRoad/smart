package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author sunfujian
 * @date 2021/8/30 15:13
 */
@Data
public class AuthDetailRespDTO extends BaseDTO {
	private Integer id;

	@ApiModelProperty(value = "工号")
	private String badge;

	@ApiModelProperty(value = "姓名")
	private String personName;

	@ApiModelProperty(value = "车牌号")
	private String vehiclePlate;

	@ApiModelProperty(value = "创建时间")
	private LocalDateTime createTime;

	@ApiModelProperty(value = "权限生效时间")
	private LocalDateTime startTime;

	@ApiModelProperty(value = "权限失效时间")
	private LocalDateTime endTime;

	@ApiModelProperty(value = "员工状态")
	private Integer staffStatus;
}
