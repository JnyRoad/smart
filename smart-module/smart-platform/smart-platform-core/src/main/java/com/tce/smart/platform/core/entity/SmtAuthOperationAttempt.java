package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限操作执行尝试持久实体。
 */
@Data
@TableName("SMT_AUTH_OPERATION_ATTEMPT")
public class SmtAuthOperationAttempt {

	@TableId(value = "ID", type = IdType.INPUT)
	private Long id;

	@TableField("TARGET_ID")
	private Long targetId;

	@TableField("ATTEMPT_NO")
	private Integer attemptNo;

	@TableField("ACCESS_TYPE")
	private String accessType;

	@TableField("TASK_ID")
	private String taskId;

	@TableField("EXTERNAL_BATCH_ID")
	private String externalBatchId;

	@TableField("EXTERNAL_COMMAND_ID")
	private String externalCommandId;

	@TableField("STATUS")
	private String status;

	@TableField("LEASE_TOKEN")
	private String leaseToken;

	@TableField("LEASE_UNTIL")
	private LocalDateTime leaseUntil;

	@TableField("NEXT_ATTEMPT_AT")
	private LocalDateTime nextAttemptAt;

	@TableField("ERROR_CODE")
	private String errorCode;

	@TableField("ERROR_MESSAGE")
	private String errorMessage;

	@TableField("RESULT_EVENT_ID")
	private Long resultEventId;

	@TableField("DISPATCHED_AT")
	private LocalDateTime dispatchedAt;

	@TableField("CONFIRMED_AT")
	private LocalDateTime confirmedAt;

	@TableField("CONVERGED_AT")
	private LocalDateTime convergedAt;

	@TableField("CREATE_TIME")
	private LocalDateTime createTime;

	@TableField("UPDATE_TIME")
	private LocalDateTime updateTime;
}
