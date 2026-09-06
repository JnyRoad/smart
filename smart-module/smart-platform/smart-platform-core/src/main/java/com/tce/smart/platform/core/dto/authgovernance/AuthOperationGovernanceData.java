package com.tce.smart.platform.core.dto.authgovernance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/** 治理命令与只读投影；Web 层不得从请求体构造 Actor。 */
public final class AuthOperationGovernanceData {

	private AuthOperationGovernanceData() {
	}

	@Value
	@Builder
	public static class Actor {
		Integer userId;
		String username;
		@Builder.Default
		List<Integer> parkIds = Collections.emptyList();
		@Builder.Default
		List<String> permissions = Collections.emptyList();

		public boolean hasPermission(String permission) {
			return permission != null && permissions != null && permissions.contains(permission);
		}

		public boolean hasPark(Integer parkId) {
			return parkId != null && parkIds != null && parkIds.contains(parkId);
		}
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TargetSnapshot {
		private Long targetId;
		private Long batchId;
		private Integer parkId;
		private String resourceCoordId;
		private String resourceId;
		private Long operationVersion;
		private String state;
		private String leaseToken;
		private LocalDateTime leaseUntil;
		private LocalDateTime dispatchedAt;
		private Integer latestAttemptNo;
	}

	@Value
	@Builder
	public static class RetryCommand {
		Long targetId;
		Long expectedOperationVersion;
		Long expectedAttemptId;
		Integer expectedAttemptNo;
		String expectedState;
		String idempotencyKey;
		String reasonText;
	}

	@Value
	@Builder
	public static class ManualVerificationCommand {
		Long targetId;
		Long expectedOperationVersion;
		Long expectedAttemptId;
		String expectedState;
		String idempotencyKey;
		String observedConclusion;
		String reasonText;
		String evidenceType;
		String evidenceReference;
		String evidenceBody;
		LocalDateTime observedAt;
	}

	@Value
	@Builder
	public static class ActionResult {
		Long actionId;
		Long targetId;
		String outcome;
		String reasonCode;
		String beforeState;
		String afterState;
		boolean replay;
	}

	@Data
	@NoArgsConstructor
	public static class ReviewRow {
		private String reviewId;
		private Integer parkId;
		private String accessType;
		private String deviceId;
		private String taskKey;
		private String reason;
		private String state;
		private LocalDateTime createdAt;
	}

	@Data
	@NoArgsConstructor
	public static class ActionRow {
		private Long actionId;
		private Long targetId;
		private String actionType;
		private Integer actorUserId;
		private String actorUsername;
		private String reasonText;
		private Long expectedOperationVersion;
		private String expectedState;
		private Long expectedAttemptId;
		private Integer expectedAttemptNo;
		private String observedConclusion;
		private String beforeState;
		private String afterState;
		private String result;
		private String resultCode;
		private String evidenceType;
		private String evidenceReference;
		private String evidenceBody;
		private String evidenceSha256;
		private LocalDateTime observedAt;
		private LocalDateTime createdAt;
	}
}
