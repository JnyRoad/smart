package com.tce.smart.platform.api.dto.req.admittance;

import lombok.Data;

import java.io.Serializable;

/**
 * 访客人脸裁剪请求，仅接受原始 Base64 图片，不支持图片 URL。
 */
@Data
public class VisitorFaceCropReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 必须与一次性裁剪能力绑定的服务端草稿标识一致。 */
	private String draftId;

	/** 待裁剪图片的原始 Base64 内容。 */
	private String imageData;
}
