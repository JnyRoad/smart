package com.tce.smart.platform.service.energy;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * 能耗日用量计算必须保持纯函数，避免投影任务在异常读数时误把用量写成零。
 */
public class EnergyDailyUsageCalculatorTest {

	@Test
	public void electricityUsesMeterMultiplier() {
		assertEquals(new BigDecimal("15.000000"), EnergyDailyUsageCalculator.calculate(
				new BigDecimal("10"), new BigDecimal("15"), new BigDecimal("3")));
	}

	@Test
	public void negativeDeltaIsNotCounted() {
		assertNull(EnergyDailyUsageCalculator.calculate(new BigDecimal("15"), new BigDecimal("10"), BigDecimal.ONE));
	}

	@Test
	public void missingBoundaryIsNotCounted() {
		assertNull(EnergyDailyUsageCalculator.calculate(null, new BigDecimal("10"), BigDecimal.ONE));
	}
}
