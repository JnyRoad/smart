package com.tce.smart.platform.service.energy;

import com.tce.smart.platform.api.dto.resp.energy.ParkUtilityUsageMonthToDateRespDTO;

import java.time.LocalDate;

/** 园区能耗日投影与月累计查询服务。 */
public interface EnergyProjectionService {
	void requestProjection(String meterSource, Long meterId, LocalDate businessDate);
	/** 消费有限队列；单表失败持久化重试后抛出批次失败，调用方不能将其视为成功。 */
	void processPending();
	/** 将指定业务日请求持久入队，成功代表接受重算而非计算已经完成。 */
	void reconcile(LocalDate businessDate);
	/** 推进一个可恢复的月扫描短批，返回实际新入队数；异常会明确传播。 */
	int backfillCurrentMonthToDate();
	ParkUtilityUsageMonthToDateRespDTO getCurrentMonthToDate(Long parkId);
}
