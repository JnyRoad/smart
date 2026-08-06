package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/** 人脸登录认证过滤器所需的最小响应，只返回认证账号工号。 */
@Data
@ApiModel("内部人脸登录响应")
public class InternalStaffFaceLoginRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("认证账号工号")
	private String badge;
}
