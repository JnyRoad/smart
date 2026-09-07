package com.tce.smart.platform.dto.authoperation;

import lombok.Data;

/**
 * 权限操作目标分页请求。
 */
@Data
public class AuthOperationTargetPageQuery {
	private Long batchId;
	private Integer current;
	private Integer size;
	private String state;
	private String deviceId;
	private String subjectType;
}
