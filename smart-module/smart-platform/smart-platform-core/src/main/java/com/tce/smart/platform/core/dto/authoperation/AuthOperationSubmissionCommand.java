package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Value;

/**
 * 当前尝试的外部提交登记命令，所有写入均由租约令牌保护。
 */
@Value
@Builder(toBuilder = true)
public class AuthOperationSubmissionCommand {
	Long targetId;
	Long attemptId;
	Integer attemptNo;
	String leaseToken;
	String accessType;
	String taskId;
	String externalBatchId;
	String externalCommandId;
}
