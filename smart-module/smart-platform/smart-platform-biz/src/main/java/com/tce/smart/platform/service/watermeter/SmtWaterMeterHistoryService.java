package com.tce.smart.platform.service.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterDataUpdateDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterHisQueryDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterHistory;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:31
 */
public interface SmtWaterMeterHistoryService extends IService<SmtWaterMeterHistory> {

	/**
	 * 通过水表序号分页获取历史读数
	 *
	 * @param page
	 * @param dto
	 * @return
	 */
	IPage<SmtWaterMeterHistory> getPage(Page page, WaterMeterHisQueryDTO dto);

	/**
	 * 通过水表序号获取历史读数，导出能用上
	 *
	 * @param dto
	 * @return
	 */
	List<SmtWaterMeterHistory> getList(WaterMeterHisQueryDTO dto);

	/**
	 * 导出水表的历史读数
	 *
	 * @param waterMeterId
	 * @return
	 */
	ResponseEntity<byte[]> exportHistory(Long waterMeterId);

	/**
	 * 保存水表读数
	 *
	 * @param updateDTO
	 * @return
	 */
	Boolean saveCurrentReading(WaterMeterDataUpdateDTO updateDTO);

	/**
	 * 获取指定日期内的最大读数，如果没有，取指定日期之前的最大读数
	 *
	 * @param meterId
	 * @param firstDay
	 * @param lastDay
	 * @return
	 */
	Double getMaxMeterReading(Long meterId, LocalDate firstDay, LocalDate lastDay);

	/**
	 * 获取水表某月起始读数
	 *
	 * @param meterId
	 * @param meterMonth
	 * @return
	 */
	Double getInitMeterReading(Long meterId, LocalDate meterMonth);
}
