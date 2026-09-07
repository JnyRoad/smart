package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/** 治理动作是不可变审计记录；业务代码只允许插入和查询。 */
@Data
@TableName("SMT_AUTH_GOVERNANCE_ACTION")
public class SmtAuthGovernanceAction {

	@TableId(value = "ID", type = IdType.INPUT)
	private Long id;
	@TableField("IDEMPOTENCY_KEY")
	private String idempotencyKey;
	@TableField("SUBJECT_KEY")
	private String subjectKey;
	@TableField("TARGET_ID")
	private Long targetId;
	@TableField("ACTION_TYPE")
	private String actionType;
	@TableField("ACTOR_USER_ID")
	private Integer actorUserId;
	@TableField("ACTOR_USERNAME")
	private String actorUsername;
	@TableField("REASON_TEXT")
	private String reasonText;
	@TableField("EXPECTED_OPERATION_VERSION")
	private Long expectedOperationVersion;
	@TableField("EXPECTED_STATE")
	private String expectedState;
	@TableField("EXPECTED_ATTEMPT_ID")
	private Long expectedAttemptId;
	@TableField("EXPECTED_ATTEMPT_NO")
	private Integer expectedAttemptNo;
	@TableField("OBSERVED_CONCLUSION")
	private String observedConclusion;
	@TableField("BEFORE_STATE")
	private String beforeState;
	@TableField("AFTER_STATE")
	private String afterState;
	@TableField("REQUEST_FINGERPRINT")
	private String requestFingerprint;
	@TableField("RESULT")
	private String result;
	@TableField("RESULT_CODE")
	private String resultCode;
	@TableField("EVIDENCE_TYPE")
	private String evidenceType;
	@TableField("EVIDENCE_REFERENCE")
	private String evidenceReference;
	@TableField(value = "EVIDENCE_BODY", jdbcType = JdbcType.CLOB)
	private String evidenceBody;
	@TableField("EVIDENCE_SHA256")
	private String evidenceSha256;
	@TableField("OBSERVED_AT")
	private LocalDateTime observedAt;
	@TableField("CREATE_TIME")
	private LocalDateTime createTime;
}
