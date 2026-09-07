package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 权限操作批次持久实体。
 */
@Data
@TableName("SMT_AUTH_OPERATION_BATCH")
public class SmtAuthOperationBatch {

	@TableId(value = "ID", type = IdType.INPUT)
	private Long id;

	@TableField("PARK_ID")
	private Integer parkId;

	@TableField("IDEMPOTENCY_KEY")
	private String idempotencyKey;

	@TableField("ACTION")
	private String action;

	@TableField("SOURCE_TYPE")
	private String sourceType;

	@TableField("SOURCE_ID")
	private String sourceId;

	@TableField(value = "SELECTION_SNAPSHOT", jdbcType = JdbcType.CLOB)
	private String selectionSnapshot;

	@TableField("PAYLOAD_FINGERPRINT")
	private String payloadFingerprint;

	@TableField("EXPECTED_COUNT")
	private Integer expectedCount;

	@TableField("EXPANDED_COUNT")
	private Integer expandedCount;

	@TableField("EXPANSION_CURSOR")
	private Long expansionCursor;

	@TableField("STATUS")
	private String status;

	@TableField("FAILURE_REASON")
	private String failureReason;

	@TableField("ACCEPTED_AT")
	private LocalDateTime acceptedAt;

	@TableField("EXPANSION_FINISHED_AT")
	private LocalDateTime expansionFinishedAt;

	@TableField("DISPATCHED_AT")
	private LocalDateTime dispatchedAt;

	@TableField("CONFIRMED_AT")
	private LocalDateTime confirmedAt;

	@TableField("CONVERGED_AT")
	private LocalDateTime convergedAt;

	@TableField("FINISHED_AT")
	private LocalDateTime finishedAt;

	@TableField("DEADLINE_AT")
	private LocalDateTime deadlineAt;

	@TableField("CREATE_TIME")
	private LocalDateTime createTime;

	@TableField("UPDATE_TIME")
	private LocalDateTime updateTime;
}
