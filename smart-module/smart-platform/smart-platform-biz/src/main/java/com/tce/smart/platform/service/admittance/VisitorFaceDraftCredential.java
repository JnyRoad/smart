package com.tce.smart.platform.service.admittance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 微信授权完成后签发的短时草稿会话凭据。
 */
@Getter
@AllArgsConstructor
public class VisitorFaceDraftCredential {
	private final String draftToken;
	private final String draftId;
}
