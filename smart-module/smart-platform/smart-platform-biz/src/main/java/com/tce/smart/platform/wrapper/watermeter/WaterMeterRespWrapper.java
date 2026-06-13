package com.tce.smart.platform.wrapper.watermeter;

import cn.hutool.core.collection.CollUtil;
import com.tce.smart.common.core.constant.NumberConstants;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.DeviceTagListDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.WaterMeterRespDTO;
import com.tce.smart.platform.core.entity.SmtArea;
import com.tce.smart.platform.core.entity.SmtDeviceTag;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeter;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterConcentrator;
import com.tce.smart.platform.emun.LargeClassEnum;
import com.tce.smart.platform.emun.MeterStatusEnum;
import com.tce.smart.platform.service.SmtAreaService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterConcentratorService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterTagService;
import com.tce.smart.platform.utils.NumberUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 14:13
 */
@Component
@AllArgsConstructor
public class WaterMeterRespWrapper extends BaseWrapper<SmtWaterMeter, WaterMeterRespDTO> {

	private final SmtWaterMeterTagService tagService;
	private final SmtWaterMeterConcentratorService waterMeterConcentratorService;
	private final SmtAreaService areaService;

	@Override
	protected WaterMeterRespDTO warp(SmtWaterMeter model) {
		WaterMeterRespDTO dto = BeanUtils.transform(WaterMeterRespDTO.class, model);
		dto.setStatus(MeterStatusEnum.desc(model.getIsOnline()));
		// 设备标签
		List<SmtDeviceTag> tagList = tagService.getTagByMeterId(model.getId());
		if (CollUtil.isNotEmpty(tagList)) {
			dto.setTagList(BeanUtils.batchTransform(DeviceTagListDTO.class, tagList));
		}
		// 阀门控制
		dto.setValveStatus(model.getIsOpen());
		SmtWaterMeterConcentrator concentrator = waterMeterConcentratorService.getById(model.getConcentratorId());
		if (Objects.nonNull(concentrator)) {
			dto.setConcentratorName(concentrator.getName());
		}
		dto.setCurrentReading(NumberUtils.strFormat(dto.getCurrentReading()));
		dto.setLargeClassDesc(LargeClassEnum.desc(Integer.parseInt(model.getLargeClass())));
		if (Objects.nonNull(dto.getAreaId())) {
			List<Integer> areaIds = new LinkedList<>();
			putAreaIds(areaIds, dto.getAreaId());
			Collections.reverse(areaIds);
			dto.setAreaIds(areaIds);
		}
		return dto;
	}

	private void putAreaIds(List<Integer> areaIds, Integer areaId) {
		areaIds.add(areaId);
		SmtArea smtArea = areaService.getById(areaId);
		if (!NumberConstants.ZERO.equals(smtArea.getPid())) {
			putAreaIds(areaIds, smtArea.getPid());
		} else {
			areaIds.add(smtArea.getParkId());
		}
	}
}
