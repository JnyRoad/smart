package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Value;

/**
 * 从目标状态重算的批次进度。
 */
@Value
@Builder
public class AuthOperationProgress {
	Long batchId;
	String batchStatus;
	Integer expectedCount;
	Integer expandedCount;
	Long expansionCursor;
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
