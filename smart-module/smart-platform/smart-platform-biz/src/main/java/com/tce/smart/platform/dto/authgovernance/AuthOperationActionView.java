package com.tce.smart.platform.dto.authgovernance;

import lombok.Builder;
import lombok.Value;

/** 治理历史投影；列表不查询 evidenceBody，单条详情才返回。 */
@Value
@Builder
public class AuthOperationActionView {
	String actionId;
	String targetId;
	String actionType;
	Integer actorUserId;
	String actorUsername;
	String reasonText;
	String expectedOperationVersion;
	String expectedState;
	String expectedAttemptId;
	Integer expectedAttemptNo;
	String observedConclusion;
	String beforeState;
	String afterState;
	String result;
	String resultCode;
	String evidenceType;
	String evidenceReference;
	String evidenceBody;
	String evidenceSha256;
	String observedAt;
	String createdAt;
}
