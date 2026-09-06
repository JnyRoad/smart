package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Value;

/**
 * 外部结果证据命令，可信标记只表示证据来源，不替代外部标识和版本校验。
 */
@Value
@Builder(toBuilder = true)
public class AuthOperationReceiptCommand {
	Long targetId;
	Long attemptId;
	Integer attemptNo;
	String leaseToken;
	String accessType;
	String externalBatchId;
	String externalCommandId;
	Long operationVersion;
	String eventNamespace;
	String eventKey;
	String evidenceType;
	String resultStatus;
	String evidenceBody;
	@Builder.Default
	boolean trustedDeviceEvidence = false;
	@Builder.Default
	boolean localConverged = false;
}
