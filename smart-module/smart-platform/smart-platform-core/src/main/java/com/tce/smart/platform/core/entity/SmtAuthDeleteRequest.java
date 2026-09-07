package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 权限删除来源请求持久实体。
 */
@Data
@TableName("SMT_AUTH_DELETE_REQUEST")
public class SmtAuthDeleteRequest {

	@TableId(value = "ID", type = IdType.INPUT)
	private Long id;

	@TableField("BATCH_ID")
	private Long batchId;

	@TableField("PARK_ID")
	private Integer parkId;

	@TableField("SUBJECT_TYPE")
	private String subjectType;

	@TableField("SOURCE_TYPE")
	private String sourceType;

	@TableField("SOURCE_ROW_ID")
	private String sourceRowId;

	@TableField("SOURCE_IDENTITY_KEY")
	private String sourceIdentityKey;

	@TableField(value = "IDENTITY_SNAPSHOT", jdbcType = JdbcType.CLOB)
	private String identitySnapshot;

	@TableField("GENERATION")
	private Long generation;

	@TableField("DEADLINE_AT")
	private LocalDateTime deadlineAt;

	@TableField("STATUS")
	private String status;

	@TableField("FAILURE_REASON")
	private String failureReason;

	@TableField("FINISHED_AT")
	private LocalDateTime finishedAt;

	@TableField("CREATE_TIME")
	private LocalDateTime createTime;

	@TableField("UPDATE_TIME")
	private LocalDateTime updateTime;
}
