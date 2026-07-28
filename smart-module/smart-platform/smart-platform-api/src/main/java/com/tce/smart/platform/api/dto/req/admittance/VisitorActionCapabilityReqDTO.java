package com.tce.smart.platform.api.dto.req.admittance;

import com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction;
import lombok.Data;

import java.io.Serializable;

/** 微信访客草稿会话换取一次性业务动作 capability。 */
@Data
public class VisitorActionCapabilityReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String draftId;

	private VisitorActionCapabilityAction action;

	/** 文档图片的 SHA-256 摘要；仅 DOCUMENT_UPLOAD 需要，避免票据被改用于另一张图片。 */
	private String payloadHash;
}
