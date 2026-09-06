package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 外部权限结果证据持久实体。
 */
@Data
@TableName("SMT_AUTH_RESULT_EVENT")
public class SmtAuthResultEvent {

	@TableId(value = "ID", type = IdType.INPUT)
	private Long id;

	@TableField("TARGET_ID")
	private Long targetId;

	@TableField("ATTEMPT_ID")
	private Long attemptId;

	@TableField("EVENT_KEY")
	private String eventKey;

	@TableField("EVENT_NAMESPACE")
	private String eventNamespace;

	@TableField("ACCESS_TYPE")
	private String accessType;

	@TableField("EXTERNAL_BATCH_ID")
	private String externalBatchId;

	@TableField("EXTERNAL_COMMAND_ID")
	private String externalCommandId;

	@TableField("OPERATION_VERSION")
	private Long operationVersion;

	@TableField("EVIDENCE_TYPE")
	private String evidenceType;

	@TableField("RESULT_STATUS")
	private String resultStatus;

	@TableField(value = "EVIDENCE_BODY", jdbcType = JdbcType.CLOB)
	private String evidenceBody;

	@TableField("RECEIVED_AT")
	private LocalDateTime receivedAt;

	@TableField("CONVERGED")
	private String converged;

	@TableField("FAILURE_REASON")
	private String failureReason;

	@TableField("CREATE_TIME")
	private LocalDateTime createTime;
}
