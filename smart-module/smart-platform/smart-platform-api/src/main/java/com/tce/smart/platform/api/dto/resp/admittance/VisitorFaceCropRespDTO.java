package com.tce.smart.platform.api.dto.resp.admittance;

import lombok.Data;

import java.io.Serializable;

/** 访客人脸裁剪结果与仅能上传该裁剪结果的一次性 capability。 */
@Data
public class VisitorFaceCropRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String imageData;

	private String uploadCapability;
}
