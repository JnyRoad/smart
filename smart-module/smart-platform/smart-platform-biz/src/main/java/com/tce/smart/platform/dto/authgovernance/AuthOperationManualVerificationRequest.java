package com.tce.smart.platform.dto.authgovernance;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 人工观察只入治理审计，不直接改变任何权限状态。 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AuthOperationManualVerificationRequest extends StrictAuthOperationGovernanceRequest {
	private String idempotencyKey;
	private String expectedOperationVersion;
	private String expectedAttemptId;
	private String expectedState;
	private String observedConclusion;
	private String reasonText;
	private AuthOperationManualEvidenceRequest evidence;
}
