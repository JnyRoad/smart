package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志表
 *
 * @author: Li.JiaJun
 * @since: 2022/7/21 14:03
 */
@Data
@TableName("smt_operate_log")
@EqualsAndHashCode(callSuper = true)
public class SmtOperateLog extends Model<SmtOperateLog> {
	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * 功能类型
	 */
	private Integer code;
	/**
	 * 功能描述
	 */
	private String codeDesc;
	/**
	 * 操作目标
	 */
	private Long targetId;
	/**
	 * 操作动作
	 */
	private Integer action;
	/**
	 * 操作人
	 */
	private String createUserName;
	/**
	 * 操作用户ID
	 */
	@TableField(value = "CREATE_USER_ID", fill = FieldFill.INSERT)
	private Integer createUserId;
	/**
	 * 操作时间
	 */
	@TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
	private LocalDateTime createTime;
}
