package com.tce.smart.platform.wrapper.watermeter;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.watermeter.WaterMeterConcentratorRespDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterConcentrator;
import com.tce.smart.platform.emun.MeterStatusEnum;
import org.springframework.stereotype.Component;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 14:08
 */
@Component
public class WaterMeterConcentratorRespWrapper extends BaseWrapper<SmtWaterMeterConcentrator, WaterMeterConcentratorRespDTO> {

	@Override
	protected WaterMeterConcentratorRespDTO warp(SmtWaterMeterConcentrator model) {
		WaterMeterConcentratorRespDTO dto = new WaterMeterConcentratorRespDTO();
		dto.setId(model.getId());
		dto.setName(model.getName());
		dto.setIp(model.getIp());
		dto.setPort(model.getPort());
		dto.setRemark(model.getRemark());
		dto.setParkId(model.getParkId());
		dto.setParkName(model.getParkName());
		dto.setAddress(model.getAddress());
		dto.setStatus(MeterStatusEnum.desc(model.getIsOnline()));
		return dto;
	}
}
