package com.tce.smart.platform.service.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterDataUpdateDTO;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterHisQueryDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterHistory;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:28
 */
public interface SmtEleMeterHistoryService extends IService<SmtEleMeterHistory> {

	/**
	 * 通过电表序号分页获取历史读数
	 *
	 * @param page
	 * @param dto
	 * @return
	 */
	IPage<SmtEleMeterHistory> getPage(Page page, EleMeterHisQueryDTO dto);

	/**
	 * 通过电表序号获取历史读数，导出能用上
	 *
	 * @param dto
	 * @return
	 */
	List<SmtEleMeterHistory> getList(EleMeterHisQueryDTO dto);

	/**
	 * 导出电表的历史读数
	 *
	 * @param eleMeterId
	 * @return
	 */
	ResponseEntity<byte[]> exportHistory(Long eleMeterId);

	/**
	 * 保存水表读数
	 *
	 * @param updateDTO
	 * @return
	 */
	Boolean saveCurrentReading(EleMeterDataUpdateDTO updateDTO);

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
	 * 获取电表某月起始读数
	 *
	 * @param meterId
	 * @param meterMonth
	 * @return
	 */
	Double getInitMeterReading(Long meterId, LocalDate meterMonth);
}
