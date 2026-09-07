package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Value;

/**
 * 分片续接结果。
 */
@Value
@Builder
public class AuthOperationExpansionResult {
	Long batchId;
	Long previousCursor;
	Long nextCursor;
	Integer appendedCount;
	Integer expandedCount;
	String status;
}
