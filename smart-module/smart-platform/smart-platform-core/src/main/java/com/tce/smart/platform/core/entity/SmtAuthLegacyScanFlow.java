package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 历史盘点单流控制行；它不是发送队列。 */
@Data
@TableName("SMT_AUTH_LEGACY_SCAN_FLOW")
public class SmtAuthLegacyScanFlow {
	private Long id;
	private String runId;
	private String flowKind;
	private String requestedBy;
	private String scopeFingerprint;
	private String auditTicket;
	private LocalDateTime captureCutoff;
	private Long idHighWater;
	private Long idLastId;
	private LocalDateTime updateHighWaterAt;
	private LocalDateTime updateLastAt;
	private Long updateLastId;
	private String revisitRequired;
	private Long revisitHighWaterId;
	private Long revisitLastId;
	private String activePass;
	private String idPassDone;
	private String updatePassDone;
	private String revisitPassDone;
	private Integer pageSize;
	private String leaseOwner;
	private String leaseToken;
	private LocalDateTime leaseUntil;
	private Long rowVersion;
	private String flowState;
	private String lastErrorCode;
	private String lastErrorDetail;
	private LocalDateTime lastCommitAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime completedAt;
}
