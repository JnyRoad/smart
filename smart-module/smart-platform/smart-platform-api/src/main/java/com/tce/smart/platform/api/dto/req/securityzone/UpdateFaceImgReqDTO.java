package com.tce.smart.platform.api.dto.req.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 人脸图片 更换
 *
 * @author
 * @date
 */
@Data
public class UpdateFaceImgReqDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("任务ID")
	private Long detailId;

	@ApiModelProperty("人脸图片")
	private String faceBase64;


}
