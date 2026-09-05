package com.tce.smart.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OAuth 客户端令牌撤销事务待办。
 *
 * <p>表中只保存任务 ID、客户端 ID、创建时间和下次重试时间，
 * 不保存 client secret、access token 或 refresh token。</p>
 */
@Data
@TableName("SYS_OAUTH_CLIENT_REVOKE_TASK")
public class OauthClientTokenRevocationTask implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 随机任务版本，同时作为并发恢复时的精确删除键。 */
	@TableId(value = "TASK_ID", type = IdType.INPUT)
	private String taskId;

	/** 需要吊销全部旧令牌的 OAuth 客户端标识。 */
	@TableField("CLIENT_ID")
	private String clientId;

	/** 待办创建时间，用于稳定、有界地选择最早任务。 */
	@TableField("CREATE_TIME")
	private LocalDateTime createTime;

	/** 下一次允许后台尝试的时间；失败时只向后推进，不改写原始创建时间。 */
	@TableField("NEXT_RETRY_AT")
	private LocalDateTime nextRetryAt;
}
