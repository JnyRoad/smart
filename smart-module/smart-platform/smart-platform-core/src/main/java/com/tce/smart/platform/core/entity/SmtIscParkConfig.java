package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 海康 ISC 园区绑定配置。
 */
@Data
@TableName("SMT_ISC_PARK_CONFIG")
@EqualsAndHashCode(callSuper = true)
public class SmtIscParkConfig extends Model<SmtIscParkConfig> {

	private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 本系统园区 ID。
	 */
	private Integer parkId;

	private String parkName;

	/**
	 * 传给分发服务用于路由目标 ISC 平台的园区 ID。
	 */
	private Integer dispatcherParkId;

	private String dispatcherParkName;

	/**
	 * 1-启用人员卡片同步；0-停用。
	 */
	private Integer cardSyncEnabled;

	/**
	 * 0-有效；1-已删除。
	 */
	private Integer delFlag;

	/**
	 * 有效记录唯一键，软删除后置空。
	 */
	@TableField(updateStrategy = FieldStrategy.IGNORED)
	private String activeKey;

	private String remark;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;

	private String optUser;
}
