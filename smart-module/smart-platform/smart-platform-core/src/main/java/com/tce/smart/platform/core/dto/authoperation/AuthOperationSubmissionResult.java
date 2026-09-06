package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Value;

/**
 * 外部提交登记结果。
 */
@Value
@Builder
public class AuthOperationSubmissionResult {
	Long targetId;
	Long attemptId;
	Integer attemptNo;
	String status;
	String taskId;
	String externalBatchId;
	String externalCommandId;
	boolean persisted;
}
