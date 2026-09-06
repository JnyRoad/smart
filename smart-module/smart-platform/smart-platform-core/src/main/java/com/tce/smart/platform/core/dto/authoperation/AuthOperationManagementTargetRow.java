package com.tce.smart.platform.core.dto.authoperation;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端目标分页的数据库投影，不读取完整主体快照和租约令牌。
 */
@Data
public class AuthOperationManagementTargetRow {
	private Long targetId;
	private Long requestId;
	private Long batchId;
	private Integer parkId;
	private String subjectType;
	private String subjectId;
	private String deviceId;
	private String resourceType;
	private String resourceId;
	private String action;
	private Long version;
	private String state;
	private String failureReason;
	private LocalDateTime acceptedAt;
	private LocalDateTime dispatchedAt;
	private LocalDateTime confirmedAt;
	private LocalDateTime convergedAt;
	private LocalDateTime nextAttemptAt;
}
