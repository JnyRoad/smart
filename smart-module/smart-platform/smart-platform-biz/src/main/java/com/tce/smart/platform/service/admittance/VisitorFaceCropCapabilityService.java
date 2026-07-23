package com.tce.smart.platform.service.admittance;

/**
 * 管理访客草稿会话和一次性人脸裁剪能力。
 */
public interface VisitorFaceCropCapabilityService {

	/** 微信授权成功后创建与 openId 绑定的短时草稿会话。 */
	VisitorFaceDraftCredential issueDraft(String openId);

	/** 草稿会话只能为其自身草稿换取一枚一次性裁剪能力。 */
	String issueCropCapability(String draftToken, String draftId);

	/** 原子消费裁剪能力，防止重放。 */
	void consumeCropCapability(String capability, String draftId);
}
