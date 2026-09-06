package com.tce.smart.platform.core.dto.authoperation;

import lombok.Data;

/**
 * 当前页目标的最新执行尝试投影。
 */
@Data
public class AuthOperationManagementAttemptRow {
	private Long targetId;
	private Integer attemptNo;
	private String status;
	private String externalBatchId;
	private String externalCommandId;
}
