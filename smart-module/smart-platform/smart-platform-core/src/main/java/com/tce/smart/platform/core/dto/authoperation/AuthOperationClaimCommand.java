package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Value;

/**
 * 有界领取命令。
 */
@Value
@Builder(toBuilder = true)
public class AuthOperationClaimCommand {
	Integer parkId;
	String operationQueue;
	Integer maxCount;
	Long leaseSeconds;
	/** null 保留旧入口；空集合明确表示没有候选，禁止退回广查。 */
	java.util.List<Long> targetIds;
	String accessType;
}
