package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 历史物理行的不可变 revision；物理状态在发现阶段恒为 UNKNOWN。 */
@Data
@TableName("SMT_AUTH_LEGACY_REVIEW")
public class SmtAuthLegacyReview {
	private Long id;
	private Long runFlowId;
	private String flowKind;
	private String rowKind;
	private String sourceTable;
	private String sourceRowId;
	private String legacyRef;
	private Integer revisionNo;
	private String rawColumnSetVersion;
	private String rawRowFormat;
	private String rawRowPayload;
	private String rawRowSha256;
	private String rawComplete;
	private String rawCompletenessReason;
	private String evidenceFormat;
	private String evidencePayload;
	private String evidenceSha256;
	private String revisionFingerprint;
	private LocalDateTime capturedAt;
	private Integer parkId;
	private String parkState;
	private String deviceCode;
	private Integer deviceType;
	private String accessType;
	private Integer serviceType;
	private String serviceFamily;
	private String cardNo;
	private Long staffId;
	private String iscPersonId;
	private String badge;
	private String imageId;
	private Integer action;
	private Integer status;
	private Integer taskType;
	private Integer code;
	private String relatedTaskRef;
	private String externalTaskId;
	private String identityState;
	private String residueKind;
	private String reviewState;
	private String reviewReason;
	private String physicalState;
	private LocalDateTime firstSeenAt;
	private LocalDateTime lastSeenAt;
	private Long rowVersion;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
