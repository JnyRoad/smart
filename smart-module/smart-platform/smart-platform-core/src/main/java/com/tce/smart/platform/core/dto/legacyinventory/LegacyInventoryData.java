package com.tce.smart.platform.core.dto.legacyinventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 员工历史盘点的内部命令与投影；不包含任何设备发送命令。 */
public final class LegacyInventoryData {

	private LegacyInventoryData() {
	}

	public enum FlowKind {
		CURRENT_SOURCE("SMT_STAFF_DEVICE_AUTH", "SOURCE_BASELINE", "STAFF_AUTH_RAW_V1", false),
		ISC_TASK("SMT_ISC_DEVICE_TASK", "TASK", "ISC_TASK_RAW_V1", true),
		ISC_DOWN("SMT_ISC_DOWN_RECORD", "DOWN_RECORD", "ISC_DOWN_RAW_V1", false),
		DIRECT_TASK("SMT_DEVICE_TASK", "TASK", "DIRECT_TASK_RAW_V1", true),
		DIRECT_DOWN("SMT_TASK_DOWN_RECORD", "DOWN_RECORD", "DIRECT_DOWN_RAW_V1", false);

		private final String sourceTable;
		private final String rowKind;
		private final String rawColumnSetVersion;
		private final boolean updatePass;

		FlowKind(String sourceTable, String rowKind, String rawColumnSetVersion, boolean updatePass) {
			this.sourceTable = sourceTable;
			this.rowKind = rowKind;
			this.rawColumnSetVersion = rawColumnSetVersion;
			this.updatePass = updatePass;
		}

		public String sourceTable() { return sourceTable; }
		public String rowKind() { return rowKind; }
		public String rawColumnSetVersion() { return rawColumnSetVersion; }
		public boolean hasUpdatePass() { return updatePass; }
	}

	public enum ScanPass { ID, UPDATE, REVISIT }
	public enum ScopeKind { PARK, GLOBAL_EXCEPTION }
	public enum CommitStatus { COMMITTED, STALE_LEASE, RETRYABLE_ERROR }
	public enum FinishStatus { COMPLETE, STALE_LEASE, NOT_EXHAUSTED }

	/** 仅供服务器认证层构造；HTTP 请求不得反序列化该类型。 */
	public static final class ServerResolvedScope {
		private final Integer actorUserId;
		private final String actorUsername;
		private final ScopeKind scopeKind;
		private final List<Integer> allowedParkIds;
		private final Set<String> permissions;

		private ServerResolvedScope(Integer actorUserId, String actorUsername, ScopeKind scopeKind,
				List<Integer> allowedParkIds, Set<String> permissions) {
			this.actorUserId = actorUserId;
			this.actorUsername = actorUsername;
			this.scopeKind = scopeKind;
			this.allowedParkIds = allowedParkIds == null ? Collections.emptyList()
					: Collections.unmodifiableList(new ArrayList<>(allowedParkIds));
			this.permissions = permissions == null ? Collections.emptySet()
					: Collections.unmodifiableSet(new HashSet<>(permissions));
		}

		public static ServerResolvedScope park(Integer actorUserId, String actorUsername,
				List<Integer> allowedParkIds, Set<String> permissions) {
			return new ServerResolvedScope(actorUserId, actorUsername, ScopeKind.PARK, allowedParkIds, permissions);
		}

		public static ServerResolvedScope globalException(Integer actorUserId, String actorUsername,
				Set<String> permissions) {
			return new ServerResolvedScope(actorUserId, actorUsername, ScopeKind.GLOBAL_EXCEPTION,
					Collections.emptyList(), permissions);
		}

		public Integer getActorUserId() { return actorUserId; }
		public String getActorUsername() { return actorUsername; }
		public ScopeKind getScopeKind() { return scopeKind; }
		public List<Integer> getAllowedParkIds() { return allowedParkIds; }
		public boolean hasPermission(String permission) { return permissions.contains(permission); }
	}

	@Value
	@Builder
	public static class InventoryScanRequest {
		String requestedBy;
		String scopeFingerprint;
		String auditTicket;
		int pageSize;
	}

	@Value
	@Builder
	public static class ScanFlowKey {
		Long flowId;
		String runId;
		FlowKind flowKind;
		long rowVersion;
	}

	@Value
	@Builder
	public static class ScanRun {
		String runId;
		String scopeFingerprint;
		LocalDateTime captureCutoff;
		List<ScanFlowKey> flows;
		boolean recovered;
	}

	@Value
	@Builder(toBuilder = true)
	public static class ScanCursor {
		long idLastId;
		LocalDateTime updateLastAt;
		long updateLastId;
		long revisitLastId;
	}

	@Value
	@Builder(toBuilder = true)
	public static class ScanLease {
		Long flowId;
		String runId;
		FlowKind flowKind;
		String leaseOwner;
		String leaseToken;
		LocalDateTime leaseUntil;
		long rowVersion;
		ScanPass activePass;
		ScanCursor cursor;
	}

	/** 五张来源表的联合读取行；各 flow 未拥有的字段保持 null。 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class RawCandidate {
		private Long id;
		private Long staffId;
		private Long authId;
		private Integer authType;
		private Integer parkId;
		private Integer action;
		private Integer status;
		private Integer deviceType;
		private Long startEpoch;
		private Long overEpoch;
		private LocalDateTime startAt;
		private LocalDateTime overAt;
		private String deviceCode;
		private String cardNo;
		private Integer code;
		private Long consume;
		private Integer times;
		private String general;
		private String imageId;
		private Integer serviceType;
		private String remark;
		private String iscTaskId;
		private LocalDateTime createTime;
		private LocalDateTime updateTime;
		private String optUser;
		private String badge;
		private String personId;
		private Long applyId;
		private Long batchId;
		private Long taskId;
		private Integer taskType;
		private Integer cardType;
		private String serialNo;
		private Integer evidenceParkId;
		private Integer deviceParkMin;
		private Integer deviceParkCount;
		private Long staffCandidateId;
		private Integer staffCandidateCount;
		private Long relatedTaskId;
		private Integer relatedTaskCount;
	}

	@Value
	@Builder
	public static class RawPage {
		String runId;
		FlowKind flowKind;
		ScanPass pass;
		ScanCursor expectedCursor;
		List<RawCandidate> rows;
		ScanCursor nextCursor;
		String pageFingerprint;
		LocalDateTime sourceReadAt;
		boolean passExhausted;
	}

	@Value
	@Builder
	public static class CanonicalReview {
		String legacyRef;
		String rowKind;
		String sourceTable;
		String sourceRowId;
		String rawColumnSetVersion;
		String rawPayload;
		String rawSha256;
		String evidencePayload;
		String stableEvidencePayload;
		String evidenceSha256;
		String revisionFingerprint;
		LocalDateTime capturedAt;
		Integer parkId;
		String parkState;
		String deviceCode;
		Integer deviceType;
		String accessType;
		Integer serviceType;
		String serviceFamily;
		String cardNo;
		Long staffId;
		String iscPersonId;
		String badge;
		String imageId;
		Integer action;
		Integer status;
		Integer taskType;
		Integer code;
		String relatedTaskRef;
		String externalTaskId;
		String identityState;
		String residueKind;
		String reviewState;
		String reviewReason;
		String physicalState;
	}

	@Value
	@Builder
	public static class CommitResult {
		CommitStatus status;
		long rowVersion;
		ScanPass activePass;
		ScanCursor cursor;
	}

	@Value
	@Builder
	public static class FinishResult {
		FinishStatus status;
		long rowVersion;
	}

	@Value
	@Builder
	public static class ReviewQuery {
		Integer parkId;
		String reviewState;
		Long afterId;
	}

	@Data
	@NoArgsConstructor
	public static class ReviewRow {
		private Long reviewId;
		private String legacyRef;
		private Integer revisionNo;
		private String flowKind;
		private String rowKind;
		private Integer parkId;
		private String parkState;
		private String deviceCode;
		private Integer deviceType;
		private String accessType;
		private Integer serviceType;
		private String serviceFamily;
		private Long staffId;
		private String identityState;
		private String residueKind;
		private String reviewState;
		private String reviewReason;
		private String physicalState;
		private LocalDateTime firstSeenAt;
		private LocalDateTime lastSeenAt;
	}

	@Value
	@Builder
	public static class ReviewPage {
		List<ReviewRow> rows;
		Long nextAfterId;
	}
}
