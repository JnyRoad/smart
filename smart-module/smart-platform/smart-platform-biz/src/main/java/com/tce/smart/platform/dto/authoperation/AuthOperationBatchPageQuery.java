package com.tce.smart.platform.dto.authoperation;

import lombok.Data;

/**
 * 权限操作批次分页请求。
 */
@Data
public class AuthOperationBatchPageQuery {
	private Integer current;
	private Integer size;
	private Integer parkId;
	private String action;
	private String status;
	private String sourceType;
	private String sourceId;
}
