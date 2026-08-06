package com.tce.smart.platform.core.mapper.energy;

import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;

/** 单表计日锁锚点，所有投影路径先锁该行。 */
public interface SmtEnergyMeterDayLockMapper {
	int ensureAnchor(@Param("meterSource") String meterSource, @Param("meterId") Long meterId, @Param("statDate") LocalDate statDate);
	int lockForUpdate(@Param("meterSource") String meterSource, @Param("meterId") Long meterId, @Param("statDate") LocalDate statDate);
}
