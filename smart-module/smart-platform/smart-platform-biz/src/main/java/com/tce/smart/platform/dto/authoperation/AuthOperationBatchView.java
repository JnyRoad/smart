package com.tce.smart.platform.dto.authoperation;

import lombok.Builder;
import lombok.Value;

/**
 * 权限操作批次列表项。
 */
@Value
@Builder
public class AuthOperationBatchView {
	String batchId;
	Integer parkId;
	String action;
	String sourceType;
	String sourceId;
	String status;
	String failureReason;
	Integer expectedCount;
	Integer expandedCount;
	String acceptedAt;
	String expansionFinishedAt;
	String updatedAt;
}
