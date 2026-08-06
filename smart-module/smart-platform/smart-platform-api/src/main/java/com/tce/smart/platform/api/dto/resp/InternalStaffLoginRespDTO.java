package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * UPMS 手机号登录补建账号所需的最小员工资料。
 */
@Data
@ApiModel("内部手机号登录员工响应")
public class InternalStaffLoginRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("员工工号")
	private String badge;

	@ApiModelProperty("证件号末六位，仅用于初始化密码")
	private String certNoLast6;
}
