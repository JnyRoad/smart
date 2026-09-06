package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Value;

/**
 * 权限操作受理命令，字段创建后不可变。
 */
@Value
@Builder(toBuilder = true)
public class AuthOperationSubmitCommand {
	Integer parkId;
	String idempotencyKey;
	String action;
	String sourceType;
	String sourceId;
	String selectionSnapshot;
	String payloadFingerprint;
	Integer expectedCount;
}
