package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 员工本人修改手机号的内部请求。
 */
@Data
@ApiModel("内部员工手机号更新请求")
public class InternalStaffPhoneUpdateReqDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("员工工号")
	private String badge;

	@ApiModelProperty("新手机号")
	private String phone;
}
