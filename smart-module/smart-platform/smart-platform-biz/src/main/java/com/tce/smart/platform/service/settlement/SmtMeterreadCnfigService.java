package com.tce.smart.platform.service.settlement;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.MeterreadConfigReqDTO;
import com.tce.smart.platform.core.dto.meter.MeterReadConfigDTO;
import com.tce.smart.platform.core.entity.SmtMeterreadConfig;

import java.util.Date;

/**
 *
 * 水电结算日配置表
 * @author fushiping
 * @date 2021-04-08 16:25:32
 */
public interface SmtMeterreadCnfigService extends IService<SmtMeterreadConfig> {

	/**
	 * 编辑配置
	 * @param reqDTO
	 * @return
	 */
	Boolean editConfig(MeterreadConfigReqDTO reqDTO);

	/**
	 * 根据园区获得配置
	 * @param parkId
	 * @return
	 */
	SmtMeterreadConfig getByParkId(Integer parkId);

	/**
	 * 获得结算配置
	 * @param parkId
	 * @param month
	 * @return
	 */
	SmtMeterreadConfig getCountDays(Integer parkId, Date month);

	/**
	 * 根据结算配置生成开始日期和结束日期
	 * @param meterMonth
	 * @param parkId
	 * @return
	 */
	MeterReadConfigDTO calcDate(Date meterMonth, Integer parkId);
}
