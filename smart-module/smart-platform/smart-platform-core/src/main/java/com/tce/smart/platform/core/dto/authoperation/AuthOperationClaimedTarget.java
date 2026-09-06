package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * 取得租约后的目标和尝试标识。
 */
@Value
@Builder
public class AuthOperationClaimedTarget {
	Long targetId;
	Long attemptId;
	Integer attemptNo;
	String targetKey;
	String subjectType;
	String subjectId;
	String deviceId;
	String resourceType;
	String resourceId;
	String accessType;
	Long operationVersion;
	String state;
	String leaseToken;
	LocalDateTime leaseUntil;
}
