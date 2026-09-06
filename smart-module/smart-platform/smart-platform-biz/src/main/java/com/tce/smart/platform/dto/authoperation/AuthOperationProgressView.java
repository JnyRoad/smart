package com.tce.smart.platform.dto.authoperation;

import lombok.Builder;
import lombok.Value;

/**
 * 权限操作批次的重算进度。
 */
@Value
@Builder
public class AuthOperationProgressView {
	String batchId;
	String batchStatus;
	Integer expectedCount;
	Integer expandedCount;
	String expansionCursor;
	Integer totalTargetCount;
	Integer preparingCount;
	Integer queuedCount;
	Integer executingCount;
	Integer waitingConfirmCount;
	Integer verifyingCount;
	Integer confirmedCount;
	Integer convergedCount;
	Integer failedCount;
	Integer unfinishedCount;
}
