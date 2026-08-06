package com.tce.smart.platform.core.mapper.energy;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.energy.SmtEnergyMeterDayFact;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

/**
 * 能耗单表计日事实访问接口。
 */
public interface SmtEnergyMeterDayFactMapper extends BaseMapper<SmtEnergyMeterDayFact> {
	Map<String, Object> selectActiveMeter(@Param("meterSource") String meterSource, @Param("meterId") Long meterId);
	Map<String, Object> selectLatestReadingAtOrBefore(@Param("meterSource") String meterSource, @Param("meterId") Long meterId,
											 @Param("boundary") LocalDateTime boundary);
	int mergeFact(SmtEnergyMeterDayFact fact);
	Long selectFactId(@Param("meterSource") String meterSource, @Param("meterId") Long meterId, @Param("statDate") LocalDate statDate);
	SmtEnergyMeterDayFact selectFact(@Param("meterSource") String meterSource, @Param("meterId") Long meterId, @Param("statDate") LocalDate statDate);
	List<SmtEnergyMeterDayFact> selectFactsForDate(@Param("statDate") LocalDate statDate);
	List<Map<String, Object>> selectActiveMeters(@Param("meterSource") String meterSource, @Param("afterId") Long afterId, @Param("limit") int limit);
	int existsFactOrActiveQueue(@Param("meterSource") String meterSource, @Param("meterId") Long meterId, @Param("statDate") LocalDate statDate);
	int countActiveMeters(@Param("meterSource") String meterSource, @Param("parkId") Long parkId);
}
