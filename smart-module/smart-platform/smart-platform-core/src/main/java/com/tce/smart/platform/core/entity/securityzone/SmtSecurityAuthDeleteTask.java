package com.tce.smart.platform.core.entity.securityzone;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自动删除审计与设备任务之间的不可变关联快照。
 *
 * <p>任务来源和任务 ID 组成跨标准任务表、ISC 任务表的复合定位，任务被清理后关联仍可审计。</p>
 */
@Data
@TableName("SMT_SECURITY_AUTH_DELETE_TASK")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityAuthDeleteTask extends Model<SmtSecurityAuthDeleteTask> {

	private static final long serialVersionUID = 1L;

	/** 关联记录主键。 */
	@TableId(value = "ID", type = IdType.ID_WORKER)
	private Long id;

	/** 审计主记录主键。 */
	private Long logId;

	/** 任务来源：NORMAL 或 ISC，对应数据库 TASK_SOURCE。 */
	@com.baomidou.mybatisplus.annotation.TableField("TASK_SOURCE")
	private String taskSource;

	/** 对应来源任务表的主键文本。 */
	private String taskId;

	/** 任务生成时的设备编码快照。 */
	private String deviceCode;

	/** 任务动作快照。 */
	private Integer action;
}
