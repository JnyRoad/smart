package com.tce.smart.platform.core.client.release;

import java.time.Instant;

/**
 * 单次状态转换产生的不可变审计事件。
 */
public final class ReleaseAuditEvent {

	private final String eventId;
	private final String releaseId;
	private final ReleaseAction action;
	private final String actorId;
	private final String postId;
	private final ReleaseStatus fromStatus;
	private final ReleaseStatus toStatus;
	private final Instant occurredAt;
	private final String reason;
	private final EscortMode escortMode;
	private final String positioningLockId;
	private final CardEvidence securityEvidence;
	private final CardEvidence escortEvidence;
	private final long version;

	ReleaseAuditEvent(String eventId, String releaseId, ReleaseAction action, String actorId, String postId,
			ReleaseStatus fromStatus, ReleaseStatus toStatus, Instant occurredAt, String reason,
			EscortMode escortMode, String positioningLockId, CardEvidence securityEvidence,
			CardEvidence escortEvidence, long version) {
		this.eventId = eventId;
		this.releaseId = releaseId;
		this.action = action;
		this.actorId = actorId;
		this.postId = postId;
		this.fromStatus = fromStatus;
		this.toStatus = toStatus;
		this.occurredAt = occurredAt;
		this.reason = reason;
		this.escortMode = escortMode;
		this.positioningLockId = positioningLockId;
		this.securityEvidence = securityEvidence;
		this.escortEvidence = escortEvidence;
		this.version = version;
	}

	public String getEventId() {
		return eventId;
	}

	public String getReleaseId() {
		return releaseId;
	}

	public ReleaseAction getAction() {
		return action;
	}

	public String getActorId() {
		return actorId;
	}

	public String getPostId() {
		return postId;
	}

	public ReleaseStatus getFromStatus() {
		return fromStatus;
	}

	public ReleaseStatus getToStatus() {
		return toStatus;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}

	public String getReason() {
		return reason;
	}

	public EscortMode getEscortMode() {
		return escortMode;
	}

	public String getPositioningLockId() {
		return positioningLockId;
	}

	public CardEvidence getSecurityEvidence() {
		return securityEvidence;
	}

	public CardEvidence getEscortEvidence() {
		return escortEvidence;
	}

	public long getVersion() {
		return version;
	}
}
