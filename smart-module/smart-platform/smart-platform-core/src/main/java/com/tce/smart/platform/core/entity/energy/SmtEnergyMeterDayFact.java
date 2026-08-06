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
 * 单表计日事实，保存日界读数和计算快照以支持重算。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_ENERGY_METER_DAY_FACT")
@EqualsAndHashCode(callSuper = true)
public class SmtEnergyMeterDayFact extends Model<SmtEnergyMeterDayFact> {
	private static final long serialVersionUID = 1L;

	/** 日事实主键。 */
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
	/** 业务日期。 */
	private LocalDate statDate;
	/** 日初读数来源历史标识。 */
	private Long dayStartHistoryId;
	/** 日初读数采集时间。 */
	private LocalDateTime dayStartTime;
	/** 日初读数。 */
	private BigDecimal dayStartReading;
	/** 日末读数来源历史标识。 */
	private Long dayEndHistoryId;
	/** 日末读数采集时间。 */
	private LocalDateTime dayEndTime;
	/** 日末读数。 */
	private BigDecimal dayEndReading;
	/** 计算时使用的倍率快照。 */
	private BigDecimal multiplierSnapshot;
	/** 原始读数差值。 */
	private BigDecimal rawDelta;
	/** 计算后的用量。 */
	private BigDecimal usageValue;
	/** 质量编码。 */
	private String qualityCode;
	/** 质量详情。 */
	private String qualityDetail;
	/** 输入数据哈希。 */
	private String sourceHash;
	/** 计算完成时间。 */
	private LocalDateTime calculatedAt;
}
