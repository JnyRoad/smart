package com.tce.smart.platform.service.energy;

import java.math.BigDecimal;

/** 单表计日用量的无副作用计算器。 */
public final class EnergyDailyUsageCalculator {
	private EnergyDailyUsageCalculator() { }

	/** 读数倒退或倍率无效时返回空值，调用方必须记录质量异常而不是写零。 */
	public static BigDecimal calculate(BigDecimal start, BigDecimal end, BigDecimal multiplier) {
		if (start == null || end == null || multiplier == null || multiplier.compareTo(BigDecimal.ZERO) <= 0) return null;
		BigDecimal delta = end.subtract(start);
		return delta.compareTo(BigDecimal.ZERO) < 0 ? null : delta.multiply(multiplier).setScale(6, BigDecimal.ROUND_HALF_UP);
	}
}
