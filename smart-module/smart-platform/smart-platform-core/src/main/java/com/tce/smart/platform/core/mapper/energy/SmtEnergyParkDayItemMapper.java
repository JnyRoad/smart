package com.tce.smart.platform.core.mapper.energy;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.energy.SmtEnergyParkDayItem;

/**
 * 能耗园区日汇总明细访问接口。
 */
public interface SmtEnergyParkDayItemMapper extends BaseMapper<SmtEnergyParkDayItem> {
	int mergeItem(SmtEnergyParkDayItem item);
}
