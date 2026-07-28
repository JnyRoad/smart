package com.tce.smart.platform.api.dto.req.admittance;

import lombok.Data;

import java.io.Serializable;

/**
 * 访客草稿会话换取一次性人脸裁剪能力的请求。
 */
@Data
public class VisitorFaceCropCapabilityReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 服务端签发的访客申请草稿标识，不接受工号或其他人员标识。 */
	private String draftId;
}
