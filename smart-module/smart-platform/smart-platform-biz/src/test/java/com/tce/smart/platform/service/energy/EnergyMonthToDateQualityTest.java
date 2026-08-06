package com.tce.smart.platform.service.energy;

import org.junit.Test;
import java.time.LocalDate;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import com.tce.smart.platform.core.entity.energy.SmtEnergyParkDay;
import com.tce.smart.platform.service.energy.impl.EnergyProjectionServiceImpl;
import java.math.BigDecimal;

/** MTD 不能用局部 READY 日掩盖缺失日期。 */
public class EnergyMonthToDateQualityTest {
	@Test public void missingDayIsPartial() {
		assertEquals("PARTIAL", EnergyMonthToDateQuality.evaluate(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3), Arrays.asList(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3)), Arrays.asList("READY", "READY")));
	}
	@Test public void noDaysIsNoData() { assertEquals("NO_DATA", EnergyMonthToDateQuality.evaluate(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1), Arrays.<LocalDate>asList(), Arrays.<String>asList())); }
	@Test public void unprojectedMetersFailClosed() {
		assertEquals("PARTIAL", EnergyParkDayQuality.status(1, 0, 0, 0, 1000, 1));
		assertEquals("READY", EnergyParkDayQuality.status(1000, 0, 0, 0, 1000, 1000));
	}
	@Test public void partialDayDoesNotContributeToUsage() {
		SmtEnergyParkDay ready = SmtEnergyParkDay.builder().usageValue(new BigDecimal("5")).qualitySummary("READY|included=1").build();
		SmtEnergyParkDay partial = SmtEnergyParkDay.builder().usageValue(new BigDecimal("9")).qualitySummary("PARTIAL|missing=1").build();
		assertEquals(new BigDecimal("5"), EnergyProjectionServiceImpl.sumReadyDays(Arrays.asList(ready, partial)));
	}
}
