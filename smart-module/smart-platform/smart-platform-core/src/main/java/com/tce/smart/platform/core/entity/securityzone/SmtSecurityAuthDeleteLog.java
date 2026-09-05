package com.tce.smart.platform.core.entity.securityzone;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 保密区权限自动删除判定的不可变审计快照。
 *
 * <p>人员、部门和权限组字段保存判定时的值，报表查询不回读会变化的业务主表。</p>
 */
@Data
@TableName("SMT_SECURITY_AUTH_DELETE_LOG")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityAuthDeleteLog extends Model<SmtSecurityAuthDeleteLog> {

	private static final long serialVersionUID = 1L;

	/** 审计记录主键。 */
	@TableId(value = "ID", type = IdType.ID_WORKER)
	private Long id;

	/** 判定所属园区。 */
	private Integer parkId;

	/** 判定执行时间。 */
	private LocalDateTime execTime;

	/** 员工主键快照。 */
	private Long staffId;

	/** 员工工号快照。 */
	private String staffBadge;

	/** 员工姓名快照。 */
	private String staffName;

	/** 部门名称快照。 */
	private String department;

	/** 权限组主键快照。 */
	private Integer authId;

	/** 权限组名称快照。 */
	private String authName;

	/** 判定时真实查到的最后进出时间，无记录时为空。 */
	private LocalDateTime lastSnapTime;

	/** 触发判定的业务原因。 */
	private String triggerReason;

	/** 判定或任务聚合结果。 */
	private String result;

	/** 处理说明及失败原因。 */
	private String remark;

	/** 审计记录创建时间。 */
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;
}
