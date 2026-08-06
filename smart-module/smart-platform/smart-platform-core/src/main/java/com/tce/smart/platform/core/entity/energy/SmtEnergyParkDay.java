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
 * 园区按资源和单位汇总的日能耗投影。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_ENERGY_PARK_DAY")
@EqualsAndHashCode(callSuper = true)
public class SmtEnergyParkDay extends Model<SmtEnergyParkDay> {
	private static final long serialVersionUID = 1L;

	/** 日汇总主键。 */
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
	/** 汇总用量。 */
	private BigDecimal usageValue;
	/** 纳入统计的表计数量。 */
	private Integer meterCount;
	/** 汇总质量说明。 */
	private String qualitySummary;
	/** 计算完成时间。 */
	private LocalDateTime calculatedAt;
}
