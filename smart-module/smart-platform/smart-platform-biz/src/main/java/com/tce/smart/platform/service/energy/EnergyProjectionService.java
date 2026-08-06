package com.tce.smart.platform.service.energy;

import com.tce.smart.platform.api.dto.resp.energy.ParkUtilityUsageMonthToDateRespDTO;

import java.time.LocalDate;

/** 园区能耗日投影与月累计查询服务。 */
public interface EnergyProjectionService {
	void requestProjection(String meterSource, Long meterId, LocalDate businessDate);
	void processPending();
	void reconcile(LocalDate businessDate);
	int backfillCurrentMonthToDate();
	ParkUtilityUsageMonthToDateRespDTO getCurrentMonthToDate(Long parkId);
}
