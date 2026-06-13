package com.tce.smart.platform.service.settlement.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.platform.api.dto.req.MeterreadConfigReqDTO;
import com.tce.smart.platform.core.dto.meter.MeterReadConfigDTO;
import com.tce.smart.platform.core.entity.SmtMeterreadConfig;
import com.tce.smart.platform.core.mapper.SmtMeterreadConfigMapper;
import com.tce.smart.platform.service.settlement.SmtMeterreadCnfigService;
import com.tce.smart.tool.enums.MeterreadCountTypeEnum;
import com.tce.smart.tool.util.ToolUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * 显示信息
 */
@Slf4j
@Service
public class SmtMeterreadConfigImpl extends ServiceImpl<SmtMeterreadConfigMapper, SmtMeterreadConfig> implements SmtMeterreadCnfigService {

	@Override
	public Boolean editConfig(MeterreadConfigReqDTO reqDTO) {
		SmtMeterreadConfig reConfig = this.getByParkId(reqDTO.getParkId());
		if (Objects.nonNull(reConfig)) {
			reConfig.setPreDate(reConfig.getCountDate());
			reConfig.setCountDate(reqDTO.getCountDate());
			reConfig.setType(reqDTO.getType());
			return this.updateById(reConfig);
		}
		SmtMeterreadConfig config = BeanUtils.transform(SmtMeterreadConfig.class, reqDTO);
		return this.save(config);
	}

	@Override
	public SmtMeterreadConfig getByParkId(Integer parkId) {
		return this.getOne(Wrappers.<SmtMeterreadConfig>query().lambda()
				.eq(SmtMeterreadConfig::getParkId, parkId));
	}

	@Override
	public SmtMeterreadConfig getCountDays(Integer parkId, Date month) {
		SmtMeterreadConfig config = this.getByParkId(parkId);
		if (Objects.isNull(config)) {
			return SmtMeterreadConfig.builder().countDate(25).preDate(26).build();
		}
		int countDate = config.getCountDate();
		if (MeterreadCountTypeEnum.FIXED.getCode().equals(config.getType())) {
			config.setCountDate(config.getCountDate() - 1);
			config.setPreDate(1);
		} else {
			config.setCountDate(countDate);
			config.setPreDate(countDate);
		}
		return config;
	}

	@Override
	public MeterReadConfigDTO calcDate(Date meterMonth, Integer parkId) {
		SmtMeterreadConfig config = this.getByParkId(parkId);
		MeterReadConfigDTO dto = new MeterReadConfigDTO();
		Integer lastDay = ToolUtils.getMonthLastDay(meterMonth);
		if (MeterreadCountTypeEnum.FIXED.getCode().equals(config.getType())) {
			dto.setStartDate(ToolUtils.getCalDate(meterMonth, Calendar.MONTH, 0));
			dto.setEndDate(ToolUtils.setCalDate(meterMonth, Calendar.DAY_OF_MONTH, lastDay));
		} else {
			Date startDate = ToolUtils.getCalDate(meterMonth, Calendar.MONTH, -1);
			Date endDate = ToolUtils.getCalDate(meterMonth, Calendar.MONTH, 0);
			startDate = ToolUtils.setCalDate(startDate, Calendar.DAY_OF_MONTH, config.getCountDate());
			endDate = ToolUtils.setCalDate(endDate, Calendar.DAY_OF_MONTH, config.getCountDate() - 1);
			dto.setStartDate(startDate);
			dto.setEndDate(endDate);
		}
		return dto;
	}
}