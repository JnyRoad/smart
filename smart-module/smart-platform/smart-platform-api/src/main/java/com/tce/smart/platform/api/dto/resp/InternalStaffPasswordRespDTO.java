package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 密码找回人脸比对使用的员工最小资料。
 */
@Data
@ApiModel("内部员工密码响应")
public class InternalStaffPasswordRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("员工ID")
	private Long staffId;

	@ApiModelProperty("工号")
	private String badge;

	@ApiModelProperty("人脸图片编号")
	private String facePicId;
}
