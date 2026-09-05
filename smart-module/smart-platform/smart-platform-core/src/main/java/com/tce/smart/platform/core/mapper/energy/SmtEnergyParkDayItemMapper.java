package com.tce.smart.platform.core.mapper.energy;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.energy.SmtEnergyParkDayItem;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;

/**
 * 能耗园区日汇总明细访问接口。
 */
public interface SmtEnergyParkDayItemMapper extends BaseMapper<SmtEnergyParkDayItem> {
	int mergeItem(SmtEnergyParkDayItem item);
	/** 读取事实所归属园区的规则快照，供有界扫描判断是否需要重算。 */
	SmtEnergyParkDayItem selectMeterDayItem(@Param("parkId") Long parkId, @Param("meterSource") String meterSource,
			@Param("meterId") Long meterId, @Param("statDate") LocalDate statDate);
}
