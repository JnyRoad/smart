package com.tce.smart.platform.core.mapper.energy;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.energy.SmtEnergyParkDay;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 能耗园区日汇总访问接口。
 */
public interface SmtEnergyParkDayMapper extends BaseMapper<SmtEnergyParkDay> {
	Map<String, Object> summarizeItems(@Param("parkId") Long parkId, @Param("statDate") LocalDate statDate,
								  @Param("resourceType") String resourceType, @Param("unit") String unit);
	int mergeParkDay(SmtEnergyParkDay parkDay);
	List<SmtEnergyParkDay> selectMonthToDate(@Param("parkId") Long parkId, @Param("monthStart") LocalDate monthStart,
												 @Param("today") LocalDate today);
}
