package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 权限操作目标持久实体。
 */
@Data
@TableName("SMT_AUTH_OPERATION_TARGET")
public class SmtAuthOperationTarget {

	@TableId(value = "ID", type = IdType.INPUT)
	private Long id;

	@TableField("BATCH_ID")
	private Long batchId;

	@TableField("REQUEST_ID")
	private Long requestId;

	@TableField("PARK_ID")
	private Integer parkId;

	@TableField("TARGET_KEY")
	private String targetKey;

	@TableField("SUBJECT_TYPE")
	private String subjectType;

	@TableField("SUBJECT_ID")
	private String subjectId;

	@TableField(value = "SUBJECT_SNAPSHOT", jdbcType = JdbcType.CLOB)
	private String subjectSnapshot;

	@TableField("RESOURCE_TYPE")
	private String resourceType;

	@TableField("DEVICE_ID")
	private String deviceId;

	@TableField("RESOURCE_ID")
	private String resourceId;

	@TableField("ACCESS_TYPE")
	private String accessType;

	@TableField("OPERATION_QUEUE")
	private String operationQueue;

	@TableField("ACTION")
	private String action;

	@TableField("VALID_FROM")
	private LocalDateTime validFrom;

	@TableField("VALID_TO")
	private LocalDateTime validTo;

	@TableField("OPERATION_VERSION")
	private Long operationVersion;

	@TableField("LEGACY_TASK_ID")
	private String legacyTaskId;

	@TableField("STATE")
	private String state;

	@TableField("RESULT_SUMMARY")
	private String resultSummary;

	@TableField("FAILURE_REASON")
	private String failureReason;

	@TableField("ACCEPTED_AT")
	private LocalDateTime acceptedAt;

	@TableField("DISPATCHED_AT")
	private LocalDateTime dispatchedAt;

	@TableField("CONFIRMED_AT")
	private LocalDateTime confirmedAt;

	@TableField("CONVERGED_AT")
	private LocalDateTime convergedAt;

	@TableField("LEASE_TOKEN")
	private String leaseToken;

	@TableField("LEASE_UNTIL")
	private LocalDateTime leaseUntil;

	@TableField("NEXT_ATTEMPT_AT")
	private LocalDateTime nextAttemptAt;

	@TableField("CREATE_TIME")
	private LocalDateTime createTime;

	@TableField("UPDATE_TIME")
	private LocalDateTime updateTime;
}
