package com.tce.smart.platform.dto.authoperation;

import lombok.Builder;
import lombok.Value;

/**
 * 权限操作目标列表项及其最新尝试摘要。
 */
@Value
@Builder
public class AuthOperationTargetView {
	String targetId;
	String requestId;
	String batchId;
	Integer parkId;
	String subjectType;
	String subjectId;
	String deviceId;
	String resourceType;
	String resourceId;
	String action;
	String version;
	String state;
	String failureReason;
	String acceptedAt;
	String dispatchedAt;
	String confirmedAt;
	String convergedAt;
	String nextAttemptAt;
	Integer latestAttemptNo;
	String latestAttemptStatus;
	String latestExternalBatchId;
	String latestExternalCommandId;
}
