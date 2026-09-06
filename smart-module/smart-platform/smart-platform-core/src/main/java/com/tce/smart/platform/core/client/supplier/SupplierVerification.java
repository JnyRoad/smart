package com.tce.smart.platform.core.client.supplier;

import java.time.Instant;

/**
 * 一次短时厂牌核验的不可变结果。
 *
 * 核验绑定服务端操作人、岗位、区域、资格身份和人员区域状态版本；登记时仍需重验。
 */
public final class SupplierVerification {

	private final String verificationId;
	private final String operatorId;
	private final String postId;
	private final String areaId;
	private final SupplierQualificationSnapshot qualificationSnapshot;
	private final SupplierPresence presence;
	private final long presenceVersion;
	private final Instant verifiedAt;
	private final Instant expiresAt;

	SupplierVerification(String verificationId, String operatorId, String postId, String areaId,
			SupplierQualificationSnapshot qualificationSnapshot, SupplierPresence presence,
			long presenceVersion, Instant verifiedAt, Instant expiresAt) {
		this.verificationId = verificationId;
		this.operatorId = operatorId;
		this.postId = postId;
		this.areaId = areaId;
		this.qualificationSnapshot = qualificationSnapshot;
		this.presence = presence;
		this.presenceVersion = presenceVersion;
		this.verifiedAt = verifiedAt;
		this.expiresAt = expiresAt;
	}

	public String getVerificationId() {
		return verificationId;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public String getPostId() {
		return postId;
	}

	public String getAreaId() {
		return areaId;
	}

	public SupplierQualificationSnapshot getQualificationSnapshot() {
		return qualificationSnapshot;
	}

	public SupplierPresence getPresence() {
		return presence;
	}

	public long getPresenceVersion() {
		return presenceVersion;
	}

	public Instant getVerifiedAt() {
		return verifiedAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}
}
