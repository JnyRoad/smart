package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/** 后台修改员工手机号的最小请求，目标员工必须再经服务端园区范围确认。 */
@Data
@ApiModel("后台员工手机号修改请求")
public class AdminStaffPhoneUpdateReqDTO {

	@ApiModelProperty(value = "员工主键", required = true)
	private Long staffId;

	@ApiModelProperty(value = "新手机号", required = true)
	private String newPhone;
}
