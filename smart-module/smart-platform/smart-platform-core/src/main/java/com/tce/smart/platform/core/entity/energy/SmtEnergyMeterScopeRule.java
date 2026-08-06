package com.tce.smart.platform.core.entity.energy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 能耗统计范围规则，按有效期和版本记录表计是否纳入汇总。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_ENERGY_METER_SCOPE_RULE")
@EqualsAndHashCode(callSuper = true)
public class SmtEnergyMeterScopeRule extends Model<SmtEnergyMeterScopeRule> {
	private static final long serialVersionUID = 1L;

	/** 规则主键。 */
	@TableId(value = "ID", type = IdType.ID_WORKER)
	private Long id;
	/** 所属园区标识。 */
	private Long parkId;
	/** 表计来源，ELE 或 WATER。 */
	private String meterSource;
	/** 表计标识。 */
	private Long meterId;
	/** 资源类型。 */
	private String resourceType;
	/** 计量单位。 */
	private String unit;
	/** 生效业务日期。 */
	private LocalDate effectiveStartDate;
	/** 失效业务日期，空值表示持续有效。 */
	private LocalDate effectiveEndDate;
	/** 是否纳入统计，1 表示纳入。 */
	private Integer includeFlag;
	/** 计量组标识。 */
	private Long meterGroupId;
	/** 父表计标识。 */
	private Long parentMeterId;
	/** 规则版本。 */
	private Integer ruleVersion;
	/** 规则决策原因。 */
	private String reason;
	/** 创建时间。 */
	private LocalDateTime createTime;
	/** 修改时间。 */
	private LocalDateTime updateTime;
}
