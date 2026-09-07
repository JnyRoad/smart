package com.tce.smart.platform.core.client.release;

import java.time.Instant;

/**
 * 真实卡证服务已核验后生成的不可变证明快照。
 *
 * 本领域层只校验快照与当前动作的绑定关系，不负责读卡、签发或联网查证。
 */
public final class CardEvidence {

	private final String evidenceId;
	private final CardRole role;
	private final String holderId;
	private final String releaseId;
	private final String postId;
	private final ReleaseAction action;
	private final String operatorId;
	private final Instant verifiedAt;
	private final Instant validUntil;

	private CardEvidence(String evidenceId, CardRole role, String holderId, String releaseId, String postId,
			ReleaseAction action, String operatorId, Instant verifiedAt, Instant validUntil) {
		this.evidenceId = evidenceId;
		this.role = role;
		this.holderId = holderId;
		this.releaseId = releaseId;
		this.postId = postId;
		this.action = action;
		this.operatorId = operatorId;
		this.verifiedAt = verifiedAt;
		this.validUntil = validUntil;
	}

	public static CardEvidence verified(String evidenceId, CardRole role, String holderId, String releaseId,
			String postId, ReleaseAction action, String operatorId, Instant verifiedAt, Instant validUntil) {
		return new CardEvidence(evidenceId, role, holderId, releaseId, postId, action, operatorId,
				verifiedAt, validUntil);
	}

	public String getEvidenceId() {
		return evidenceId;
	}

	public CardRole getRole() {
		return role;
	}

	public String getHolderId() {
		return holderId;
	}

	public String getReleaseId() {
		return releaseId;
	}

	public String getPostId() {
		return postId;
	}

	public ReleaseAction getAction() {
		return action;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public Instant getVerifiedAt() {
		return verifiedAt;
	}

	public Instant getValidUntil() {
		return validUntil;
	}
}
