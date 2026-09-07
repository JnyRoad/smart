package com.tce.smart.platform.dto.authgovernance;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 单个目标的乐观版本门禁，所有长整型 ID 用字符串传输。 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AuthOperationRetryItem extends StrictAuthOperationGovernanceRequest {
	private String targetId;
	private String expectedOperationVersion;
	private String expectedAttemptId;
	private Integer expectedAttemptNo;
	private String expectedState;
}
