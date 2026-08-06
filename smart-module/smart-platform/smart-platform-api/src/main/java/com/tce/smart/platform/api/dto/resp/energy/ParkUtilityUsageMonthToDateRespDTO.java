package com.tce.smart.platform.api.dto.resp.energy;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 园区从本月一日至调用时刻的水、电分项能耗。 */
@Data
public class ParkUtilityUsageMonthToDateRespDTO {
	private Long parkId;
	private LocalDate businessMonth;
	private LocalDateTime asOf;
	private String timezone;
	private UtilityUsageItemRespDTO water;
	private UtilityUsageItemRespDTO electricity;
}
