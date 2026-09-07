package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * 管理端目标分页的服务端过滤条件。
 */
@Value
@Builder
public class AuthOperationManagementTargetFilter {
	Long batchId;
	Integer parkId;
	List<String> states;
	String deviceId;
	String subjectType;
}
