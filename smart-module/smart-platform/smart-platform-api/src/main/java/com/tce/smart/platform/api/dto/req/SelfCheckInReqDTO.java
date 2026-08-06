package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 当前认证员工的宿舍入住选择。
 *
 * 员工身份资料由服务端依据认证工号读取，浏览器只能提交宿舍和床位选择。
 */
@Data
public class SelfCheckInReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "园区ID", required = true)
	@NotNull(message = "园区不能为空")
	private Integer parkId;

	@ApiModelProperty(value = "楼栋ID", required = true)
	@NotNull(message = "楼栋不能为空")
	private Integer dormitoryId;

	@ApiModelProperty(value = "楼层ID")
	private Integer floorId;

	@ApiModelProperty(value = "房间ID")
	private Integer roomId;

	@ApiModelProperty(value = "床位ID")
	private Integer bedId;

	@ApiModelProperty(value = "房间类型", required = true)
	@NotNull(message = "房间类型不能为空")
	private Integer roomType;
}
