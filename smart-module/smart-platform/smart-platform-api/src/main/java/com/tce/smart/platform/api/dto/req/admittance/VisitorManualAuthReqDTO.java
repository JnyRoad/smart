package com.tce.smart.platform.api.dto.req.admittance;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 管理端为访客申请手动下发人员权限的请求。
 */
@Data
public class VisitorManualAuthReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "入厂申请单 ID", required = true)
	private Long applyId;

	@ApiModelProperty("本申请中的人员 ID；与 vehicleId 二选一")
	private Long fellowId;

	@ApiModelProperty("本申请中的车辆 ID；当前版本明确不支持车辆下发")
	private Long vehicleId;

	@ApiModelProperty(value = "人员权限组 ID", required = true)
	private List<Integer> authIds;
}
