package com.tce.smart.platform.core.client.supplier;

import java.time.Instant;

/**
 * 单次明确进入或离开产生的不可变事件。
 */
public final class SupplierPassageEvent {

	private final String eventId;
	private final String verificationId;
	private final String operatorId;
	private final String postId;
	private final String areaId;
	private final SupplierDirection direction;
	private final Instant occurredAt;
	private final long version;
	private final SupplierQualificationSnapshot qualificationSnapshot;

	SupplierPassageEvent(String eventId, String verificationId, String operatorId, String postId,
			String areaId, SupplierDirection direction, Instant occurredAt, long version,
			SupplierQualificationSnapshot qualificationSnapshot) {
		this.eventId = eventId;
		this.verificationId = verificationId;
		this.operatorId = operatorId;
		this.postId = postId;
		this.areaId = areaId;
		this.direction = direction;
		this.occurredAt = occurredAt;
		this.version = version;
		this.qualificationSnapshot = qualificationSnapshot;
	}

	public String getEventId() {
		return eventId;
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

	public SupplierDirection getDirection() {
		return direction;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}

	public long getVersion() {
		return version;
	}

	public SupplierQualificationSnapshot getQualificationSnapshot() {
		return qualificationSnapshot;
	}
}
