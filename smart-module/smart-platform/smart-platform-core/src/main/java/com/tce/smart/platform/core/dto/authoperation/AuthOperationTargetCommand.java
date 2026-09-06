package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * 一个目标分片的不可变输入，设备与资源分别保留。
 */
@Value
@Builder(toBuilder = true)
public class AuthOperationTargetCommand {
	Long id;
	Long requestId;
	Integer parkId;
	String targetKey;
	String subjectType;
	String subjectId;
	String subjectSnapshot;
	String resourceType;
	String deviceId;
	String resourceId;
	String accessType;
	String operationQueue;
	String action;
	LocalDateTime validFrom;
	LocalDateTime validTo;
	Long operationVersion;
	String legacyTaskId;
}
