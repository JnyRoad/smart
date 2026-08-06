package com.tce.smart.platform.api.dto.resp.energy;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 园区单类公用能耗累计结果。 */
@Data
public class UtilityUsageItemRespDTO {
	/** 仅对已就绪日期求和的用量；全月无有效数据时为空。 */
	private BigDecimal usageValue;
	/** 单位，水为 m3，电为 kWh。 */
	private String unit;
	/** READY、PARTIAL 或 NO_DATA。 */
	private String qualityStatus;
	/** 最近一次日投影计算时间。 */
	private LocalDateTime lastCalculatedAt;
}
