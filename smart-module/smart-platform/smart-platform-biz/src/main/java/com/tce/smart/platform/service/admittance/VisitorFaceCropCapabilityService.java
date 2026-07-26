package com.tce.smart.platform.service.admittance;

import com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction;

/**
 * 管理访客草稿会话和一次性人脸裁剪能力。
 */
public interface VisitorFaceCropCapabilityService {

	/** 微信授权成功后创建与 openId 绑定的短时草稿会话。 */
	VisitorFaceDraftCredential issueDraft(String openId);

	/** 微信身份信息仅保存在服务端草稿中，浏览器不得接收 unionId。 */
	VisitorFaceDraftCredential issueDraft(String openId, String unionId);

	/** 验证草稿会话后取得服务端保存的 unionId，用于最终申请入库。 */
	String resolveUnionId(String draftToken, String draftId);

	/** 仅允许微信 OAuth 草稿在限速窗口内读取无个人信息的访客表单选项。 */
	void assertStaticOptionAccess(String draftToken, String draftId);

	/** 草稿完成接待人精确检索后暂存服务端选中的接待人，提交时不再信任浏览器回传的工号与电话。 */
	void rememberReceptionistSelection(String draftId, String receptionistBadge, String receptionistName, String receptionistPhone);

	/** 消费与草稿绑定的一次性接待人选择，防止把搜索结果替换成任意员工。 */
	VisitorReceptionistSelection consumeReceptionistSelection(String draftToken, String draftId);

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

	/** 只在服务端草稿中短暂保存的接待人最小选择结果。 */
	final class VisitorReceptionistSelection {
		private final String receptionistBadge;
		private final String receptionistName;
		private final String receptionistPhone;

		public VisitorReceptionistSelection(String receptionistBadge, String receptionistName, String receptionistPhone) {
			this.receptionistBadge = receptionistBadge;
			this.receptionistName = receptionistName;
			this.receptionistPhone = receptionistPhone;
		}

		public String getReceptionistBadge() {
			return receptionistBadge;
		}

		public String getReceptionistName() {
			return receptionistName;
		}

		public String getReceptionistPhone() {
			return receptionistPhone;
		}
	}
}
