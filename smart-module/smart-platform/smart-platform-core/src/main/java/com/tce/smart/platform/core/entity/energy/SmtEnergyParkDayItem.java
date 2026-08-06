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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 园区日汇总明细，固化参与汇总时采用的范围规则决策。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_ENERGY_PARK_DAY_ITEM")
@EqualsAndHashCode(callSuper = true)
public class SmtEnergyParkDayItem extends Model<SmtEnergyParkDayItem> {
	private static final long serialVersionUID = 1L;

	/** 日明细主键。 */
	@TableId(value = "ID", type = IdType.ID_WORKER)
	private Long id;
	/** 所属园区标识。 */
	private Long parkId;
	/** 业务日期。 */
	private LocalDate statDate;
	/** 资源类型。 */
	private String resourceType;
	/** 计量单位。 */
	private String unit;
	/** 表计来源，ELE 或 WATER。 */
	private String meterSource;
	/** 表计标识。 */
	private Long meterId;
	/** 对应单表计日事实标识。 */
	private Long meterDayFactId;
	/** 日用量。 */
	private BigDecimal usageValue;
	/** 使用的范围规则标识。 */
	private Long ruleId;
	/** 使用的范围规则版本。 */
	private Integer ruleVersion;
	/** 范围规则决策。 */
	private String ruleDecision;
	/** 范围规则决策原因。 */
	private String ruleReason;
	/** 汇总计算完成时间。 */
	private LocalDateTime calculatedAt;
}
