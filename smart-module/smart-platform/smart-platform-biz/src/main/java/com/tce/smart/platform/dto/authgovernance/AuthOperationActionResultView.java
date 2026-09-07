package com.tce.smart.platform.dto.authgovernance;

import lombok.Builder;
import lombok.Value;

/** 治理动作结果；ID 以字符串输出避免 JavaScript 精度损失。 */
@Value
@Builder
public class AuthOperationActionResultView {
	String actionId;
	String targetId;
	String outcome;
	String reasonCode;
	String beforeState;
	String afterState;
	boolean replay;
}
