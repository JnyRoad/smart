package com.tce.smart.platform.service.energy;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 月累计完整性判断：任何缺失业务日都不能声称 READY。 */
public final class EnergyMonthToDateQuality {
	private EnergyMonthToDateQuality() { }
	public static String evaluate(LocalDate monthStart, LocalDate today, List<LocalDate> presentDates, List<String> statuses) {
		if (presentDates == null || presentDates.isEmpty()) return "NO_DATA";
		Set<LocalDate> unique = new HashSet<>(presentDates);
		for (LocalDate date = monthStart; !date.isAfter(today); date = date.plusDays(1)) if (!unique.contains(date)) return "PARTIAL";
		for (String status : statuses) if (!"READY".equals(status)) return "PARTIAL";
		return "READY";
	}
}
