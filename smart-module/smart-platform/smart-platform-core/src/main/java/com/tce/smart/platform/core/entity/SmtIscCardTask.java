package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 海康 ISC 卡片资源同步任务。
 */
@Data
@TableName("SMT_ISC_CARD_TASK")
@EqualsAndHashCode(callSuper = true)
public class SmtIscCardTask extends Model<SmtIscCardTask> {

	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 1-新增卡片；2-删除卡片。
	 */
	private Integer action;

	/**
	 * 数值越大越先执行。
	 */
	private Integer priority;

	/**
	 * 0-初始化；1-成功；2-失败；3-执行中；4-取消。
	 */
	private Integer status;

	/**
	 * 分发服务路由园区 ID。
	 */
	private Integer parkId;

	/**
	 * 本系统业务来源类型，例如 STAFF。
	 */
	private String sourceType;

	/**
	 * 本系统业务来源 ID。
	 */
	private Long sourceId;

	/**
	 * 员工工号 / ISC 工号。
	 */
	private String badge;

	/**
	 * 目标 ISC 平台解析到的人员 ID。
	 */
	private String personId;

	/**
	 * 海康 ISC 实体卡号。
	 */
	private String cardNo;

	/**
	 * 未完成任务唯一键，成功或取消后清空。
	 */
	@TableField(updateStrategy = FieldStrategy.IGNORED)
	private String activeKey;

	/**
	 * 当前执行租约标识。
	 */
	@TableField(updateStrategy = FieldStrategy.IGNORED)
	private String leaseToken;

	/**
	 * 同一业务来源、同一园区当前执行任务唯一键。
	 */
	@TableField(updateStrategy = FieldStrategy.IGNORED)
	private String runningKey;

	private Integer code;

	private String remark;

	private Long consume;

	private Integer times;

	/**
	 * 下次可执行时间，秒级时间戳。
	 */
	private Long overTime;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	private String optUser;
}
