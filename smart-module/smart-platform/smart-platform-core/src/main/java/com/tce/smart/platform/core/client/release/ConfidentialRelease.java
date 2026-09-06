package com.tce.smart.platform.core.client.release;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 保密物品放行单的不可变持久化快照。
 */
public final class ConfidentialRelease {

	private final String releaseId;
	private final String applicantId;
	private final String assignedApproverId;
	private final String title;
	private final String reason;
	private final List<String> materials;
	private final List<String> sealCodes;
	private final String originPostId;
	private final String destinationPostId;
	private final ReleaseStatus status;
	private final EscortMode escortMode;
	private final String positioningLockId;
	private final long version;
	private final List<ReleaseAuditEvent> auditTrail;

	ConfidentialRelease(String releaseId, String applicantId, String assignedApproverId, String title,
			String reason, List<String> materials, List<String> sealCodes, String originPostId,
			String destinationPostId, ReleaseStatus status, EscortMode escortMode, String positioningLockId,
			long version, List<ReleaseAuditEvent> auditTrail) {
		this.releaseId = releaseId;
		this.applicantId = applicantId;
		this.assignedApproverId = assignedApproverId;
		this.title = title;
		this.reason = reason;
		this.materials = immutableList(materials);
		this.sealCodes = immutableList(sealCodes);
		this.originPostId = originPostId;
		this.destinationPostId = destinationPostId;
		this.status = status;
		this.escortMode = escortMode;
		this.positioningLockId = positioningLockId;
		this.version = version;
		this.auditTrail = immutableList(auditTrail);
	}

	public String getReleaseId() {
		return releaseId;
	}

	public String getApplicantId() {
		return applicantId;
	}

	public String getAssignedApproverId() {
		return assignedApproverId;
	}

	public String getTitle() {
		return title;
	}

	public String getReason() {
		return reason;
	}

	public List<String> getMaterials() {
		return materials;
	}

	public List<String> getSealCodes() {
		return sealCodes;
	}

	public String getOriginPostId() {
		return originPostId;
	}

	public String getDestinationPostId() {
		return destinationPostId;
	}

	public ReleaseStatus getStatus() {
		return status;
	}

	public EscortMode getEscortMode() {
		return escortMode;
	}

	public String getPositioningLockId() {
		return positioningLockId;
	}

	public long getVersion() {
		return version;
	}

	public List<ReleaseAuditEvent> getAuditTrail() {
		return auditTrail;
	}

	private static <T> List<T> immutableList(List<T> values) {
		if (values == null || values.isEmpty()) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(new ArrayList<>(values));
	}
}
