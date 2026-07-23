package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/** 人脸登录内部请求，仅保留人脸照片与设备编号。 */
@Data
@ApiModel("内部人脸登录请求")
public class InternalStaffFaceLoginReqDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("现场人脸照片 Base64")
	private String facePic;

	@ApiModelProperty("设备编号")
	private String deviceNo;
}
