package com.tce.smart.platform.dto.authoperation;

import lombok.Builder;
import lombok.Value;

/**
 * 权限操作批次详情及其重算进度。
 */
@Value
@Builder
public class AuthOperationBatchDetailView {
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
	AuthOperationProgressView progress;
}
