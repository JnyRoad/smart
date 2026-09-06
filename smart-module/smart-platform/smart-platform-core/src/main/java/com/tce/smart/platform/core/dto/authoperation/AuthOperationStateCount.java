package com.tce.smart.platform.core.dto.authoperation;

import lombok.Data;

/**
 * 批次目标按状态聚合的计数行。
 */
@Data
public class AuthOperationStateCount {
	private String state;
	private Integer targetCount;
}
