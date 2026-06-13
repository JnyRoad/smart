package com.tce.smart.platform.core.mapper.watermeter;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.meter.MeterReadHisDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterHistory;
import org.apache.ibatis.annotations.Param;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:28
 */
public interface SmtEleMeterHistoryMapper extends BaseMapper<SmtEleMeterHistory> {

	/**
	 * 获取指定日期的最大读数
	 *
	 * @param page
	 * @param meterId
	 * @param firstDay
	 * @param lastDay
	 * @return
	 */
	IPage<MeterReadHisDTO> maxReading(Page page, @Param("meterId") Long meterId, @Param("firstDay") String firstDay, @Param("lastDay") String lastDay);

	/**
	 * 获取水表某月起始读数
	 *
	 * @param page
	 * @param meterId
	 * @param meterMonth
	 * @return
	 */
	IPage<MeterReadHisDTO> initReading(Page page, @Param("meterId") Long meterId, @Param("meterMonth") String meterMonth);
}
