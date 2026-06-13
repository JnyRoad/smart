package com.tce.smart.platform.wrapper.watermeter;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.watermeter.WaterMeterChangeRespDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterChange;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Li.JiaJun
 * @since 2022/5/12 11:15
 */
@Component
@AllArgsConstructor
public class WaterMeterChangeRespWrapper extends BaseWrapper<SmtWaterMeterChange, WaterMeterChangeRespDTO> {
	@Override
	protected WaterMeterChangeRespDTO warp(SmtWaterMeterChange model) {
		return BeanUtils.transform(WaterMeterChangeRespDTO.class, model);
	}
}
