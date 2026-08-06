package com.tce.smart.platform.core.mapper.watermeter;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.meter.MeterReadHisDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterHistory;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:31
 */
public interface SmtWaterMeterHistoryMapper extends BaseMapper<SmtWaterMeterHistory> {

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

	/** 按设备采集时间取得当前最新历史，采集时间相同以主键排序。 */
	SmtWaterMeterHistory selectLatestByCollectTime(@Param("meterId") Long meterId);

	/** 取得待写入读数之前的历史，避免迟到读数与未来读数比较。 */
	SmtWaterMeterHistory selectPreviousByCollectTime(@Param("meterId") Long meterId,
											   @Param("collectTime") LocalDateTime collectTime, @Param("historyId") Long historyId);

	/** 锁定主表行，使同一水表的读数事务按采集时间顺序串行化。 */
	Long lockMeterForUpdate(@Param("meterId") Long meterId);
}
