package com.tce.smart.platform.wrapper.watermeter;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.watermeter.EleMeterHisRespDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeter;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterHistory;
import com.tce.smart.platform.service.watermeter.SmtEleMeterService;
import com.tce.smart.platform.utils.NumberUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 15:20
 */
@Component
@AllArgsConstructor
@Slf4j
public class EleMeterHisRespWrapper extends BaseWrapper<SmtEleMeterHistory, EleMeterHisRespDTO> {

	private final SmtEleMeterService eleMeterService;

	@Override
	protected EleMeterHisRespDTO warp(SmtEleMeterHistory model) {
		EleMeterHisRespDTO dto = BeanUtils.transform(EleMeterHisRespDTO.class, model);
		SmtEleMeter eleMeter = eleMeterService.getById(model.getEleMeterId());
		if (Objects.nonNull(eleMeter)) {
			String reading = String.valueOf(eleMeter.getRatio() * Double.parseDouble(dto.getCurrentReading()));
			dto.setCurrentReading(NumberUtils.strFormat(reading));
		}
		return dto;
	}
}
