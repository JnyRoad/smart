package com.tce.smart.platform.wrapper.watermeter;

import cn.hutool.core.collection.CollUtil;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.DeviceTagListDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.WaterMeterValveRespDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTag;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterValve;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterValveConcentrator;
import com.tce.smart.platform.service.watermeter.SmtWaterValveConcentratorService;
import com.tce.smart.platform.service.watermeter.SmtWaterValveTagService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 11:18
 */
@Component
@Slf4j
@AllArgsConstructor
public class WaterMeterValveRespWrapper extends BaseWrapper<SmtWaterMeterValve, WaterMeterValveRespDTO> {

	private final SmtWaterValveConcentratorService waterValveConcentratorService;
	private final SmtWaterValveTagService waterValveTagService;

	@Override
	protected WaterMeterValveRespDTO warp(SmtWaterMeterValve model) {
		WaterMeterValveRespDTO respDTO = BeanUtils.transform(WaterMeterValveRespDTO.class, model);
		SmtWaterValveConcentrator valveConcentrator = waterValveConcentratorService.getById(model.getConcentratorId());
		if (valveConcentrator != null) {
			respDTO.setConcentratorName(valveConcentrator.getName());
			respDTO.setParkName(valveConcentrator.getParkName());
		}
		List<SmtDeviceTag> tagList = waterValveTagService.getTagByValveId(model.getId());
		if (CollUtil.isNotEmpty(tagList)) {
			respDTO.setTagList(BeanUtils.batchTransform(DeviceTagListDTO.class, tagList));
		}
		return respDTO;
	}
}
