package com.tce.smart.platform.wrapper.watermeter;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.watermeter.EleMeterConcentratorRespDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterConcentrator;
import com.tce.smart.platform.emun.MeterStatusEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 14:08
 */
@Component
@AllArgsConstructor
public class EleMeterConcentratorRespWrapper extends BaseWrapper<SmtEleMeterConcentrator, EleMeterConcentratorRespDTO> {

	@Override
	protected EleMeterConcentratorRespDTO warp(SmtEleMeterConcentrator model) {
		EleMeterConcentratorRespDTO dto = new EleMeterConcentratorRespDTO();
		dto.setId(model.getId());
		dto.setName(model.getName());
		dto.setIp(model.getIp());
		dto.setPort(model.getPort());
		dto.setParkId(model.getParkId());
		dto.setParkName(model.getParkName());
		dto.setRemark(model.getRemark());
		dto.setAddress(model.getAddress());
		dto.setStatus(MeterStatusEnum.desc(model.getIsOnline()));
		return dto;
	}
}
