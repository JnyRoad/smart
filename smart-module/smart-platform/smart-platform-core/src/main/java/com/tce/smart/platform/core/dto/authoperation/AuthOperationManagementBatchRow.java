package com.tce.smart.platform.core.dto.authoperation;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端批次查询的数据库投影，不包含选择快照等敏感字段。
 */
@Data
public class AuthOperationManagementBatchRow {
	private Long batchId;
	private Integer parkId;
	private String action;
	private String sourceType;
	private String sourceId;
	private String status;
	private String failureReason;
	private Integer expectedCount;
	private Integer expandedCount;
	private LocalDateTime acceptedAt;
	private LocalDateTime expansionFinishedAt;
	private LocalDateTime updatedAt;
}
