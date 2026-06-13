package com.tce.smart.platform.wrapper.watermeter;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.watermeter.WaterMeterHisRespDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterHistory;
import com.tce.smart.platform.utils.NumberUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 15:20
 */
@Component
@AllArgsConstructor
@Slf4j
public class WaterMeterHisRespWrapper extends BaseWrapper<SmtWaterMeterHistory, WaterMeterHisRespDTO> {

	@Override
	protected WaterMeterHisRespDTO warp(SmtWaterMeterHistory model) {
		WaterMeterHisRespDTO dto = BeanUtils.transform(WaterMeterHisRespDTO.class, model);
		dto.setCurrentReading(NumberUtils.strFormat(model.getCurrentReading()));
		return dto;
	}
}
