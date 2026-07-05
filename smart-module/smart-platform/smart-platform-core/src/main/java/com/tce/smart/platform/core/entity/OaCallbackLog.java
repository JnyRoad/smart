package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * OA 工作流回调审计与重放日志（spec §3.3）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oa_callback_log")
public class OaCallbackLog extends Model<OaCallbackLog> {

	/** 处理状态：已接收 */
	public static final int STATUS_RECEIVED = 0;
	/** 处理状态：全部成功 */
	public static final int STATUS_SUCCESS = 1;
	/** 处理状态：部分失败 */
	public static final int STATUS_PARTIAL_FAIL = 2;
	/** 未解决 */
	public static final int RESOLVED_NO = 0;
	/** 已解决 */
	public static final int RESOLVED_YES = 1;

	/** 主键：MyBatis-Plus 雪花 ID（Oracle 无自增） */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/** OA requestid */
	private String requestId;
	/** 完整回调报文 JSON */
	private String payload;
	private LocalDateTime receiveTime;
	/** 0=已接收 1=处理成功 2=部分失败 */
	private Integer status;
	/** 0=未解决 1=已解决 */
	private Integer resolved;
	/** 成功 handler 名逗号分隔（跳过集合，含合并值） */
	private String succeededHandlers;
	/** 失败 handler 名逗号分隔 */
	private String failedHandlers;
	/** 最后一次失败摘要 */
	private String lastError;
	/** 重放次数 */
	private Integer retryCount;
	/** 分发耗时毫秒 */
	private Long costMs;
}
