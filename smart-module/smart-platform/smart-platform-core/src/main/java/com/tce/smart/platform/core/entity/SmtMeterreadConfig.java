package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("smt_meterread_config")
@EqualsAndHashCode(callSuper = true)
public class SmtMeterreadConfig extends Model<SmtMeterreadConfig> {
	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 结算类型
	 */
	private Integer type;

	/**
	 * 结算日
	 */
	private Integer countDate;

	/**
	 * 上次结算日
	 */
	private Integer preDate;

	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	/**
	 * 修改时间
	 */
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;


}
