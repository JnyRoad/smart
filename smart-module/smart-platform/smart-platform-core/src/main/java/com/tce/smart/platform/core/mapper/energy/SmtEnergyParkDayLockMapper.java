package com.tce.smart.platform.core.mapper.energy;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/** 园区日汇总锁锚点访问接口，行锁必须在投影事务内持有。 */
public interface SmtEnergyParkDayLockMapper {
	int ensureAnchor(@Param("parkId") Long parkId, @Param("statDate") LocalDate statDate,
					 @Param("resourceType") String resourceType, @Param("unit") String unit);
	int lockForUpdate(@Param("parkId") Long parkId, @Param("statDate") LocalDate statDate,
					  @Param("resourceType") String resourceType, @Param("unit") String unit);
}
