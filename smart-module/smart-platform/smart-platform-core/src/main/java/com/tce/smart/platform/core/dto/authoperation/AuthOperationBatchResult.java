package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Value;

/**
 * 批次状态结果。
 */
@Value
@Builder
public class AuthOperationBatchResult {
	Long batchId;
	String status;
	Integer expectedCount;
	Integer expandedCount;
	Long expansionCursor;
	boolean idempotent;
}
