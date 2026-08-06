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

import java.time.LocalDateTime;

/**
 * 能耗来源事件账本，保留原始事件以支持审计和重算。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_ENERGY_INGESTION_LEDGER")
@EqualsAndHashCode(callSuper = true)
public class SmtEnergyIngestionLedger extends Model<SmtEnergyIngestionLedger> {
	private static final long serialVersionUID = 1L;

	/** 账本主键。 */
	@TableId(value = "ID", type = IdType.ID_WORKER)
	private Long id;
	/** 来源系统事件唯一标识。 */
	private String sourceEventId;
	/** 事件类型。 */
	private String eventType;
	/** 所属园区标识。 */
	private Long parkId;
	/** 表计来源，ELE 或 WATER。 */
	private String meterSource;
	/** 来源表计标识。 */
	private Long meterId;
	/** 来源事件时间。 */
	private LocalDateTime eventTime;
	/** 原始事件内容哈希。 */
	private String payloadHash;
	/** 原始事件内容。 */
	private String eventPayload;
	/** 事件入账时间。 */
	private LocalDateTime ingestedAt;
}
