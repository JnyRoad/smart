package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Value;

/**
 * 结果证据持久化结果。
 */
@Value
@Builder
public class AuthOperationReceiptResult {
	Long targetId;
	Long attemptId;
	Long eventId;
	String state;
	boolean confirmed;
	boolean converged;
	boolean duplicate;
}
