package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * 管理端批次查询的服务端过滤条件。
 */
@Value
@Builder
public class AuthOperationManagementBatchFilter {
	List<Integer> allowedParkIds;
	Integer parkId;
	String action;
	String status;
	String sourceType;
	String sourceId;
}
