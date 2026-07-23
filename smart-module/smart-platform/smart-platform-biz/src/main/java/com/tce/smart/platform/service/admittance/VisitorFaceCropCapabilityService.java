package com.tce.smart.platform.service.admittance;

import com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction;

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

	/** 草稿会话换取指定业务动作的一次性 capability。 */
	String issueActionCapability(String draftToken, String draftId, VisitorActionCapabilityAction action);

	/** 草稿会话换取与指定图片摘要绑定的一次性 capability。 */
	String issueActionCapability(String draftToken, String draftId, VisitorActionCapabilityAction action, String payloadHash);

	/** 已消费 FACE_CROP capability 后，服务端为裁剪结果派生上传 capability。 */
	String issueActionCapabilityForVerifiedDraft(String draftId, VisitorActionCapabilityAction action, String payloadHash);

	/** 原子消费指定业务动作 capability，动作与草稿任一不匹配均拒绝。 */
	void consumeActionCapability(String capability, String draftId, VisitorActionCapabilityAction action);

	/** 原子消费图片绑定动作 capability。 */
	void consumeActionCapability(String capability, String draftId, VisitorActionCapabilityAction action, String payloadHash);
}
