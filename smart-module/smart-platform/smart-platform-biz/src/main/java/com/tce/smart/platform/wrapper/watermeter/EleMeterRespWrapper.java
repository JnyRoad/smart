package com.tce.smart.platform.wrapper.watermeter;

import cn.hutool.core.collection.CollUtil;
import com.tce.smart.common.core.constant.NumberConstants;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.DeviceTagListDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.EleMeterRespDTO;
import com.tce.smart.platform.core.entity.SmtArea;
import com.tce.smart.platform.core.entity.SmtDeviceTag;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeter;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterConcentrator;
import com.tce.smart.platform.emun.MeterStatusEnum;
import com.tce.smart.platform.service.SmtAreaService;
import com.tce.smart.platform.service.watermeter.SmtEleMeterConcentratorService;
import com.tce.smart.platform.service.watermeter.SmtEleMeterTagService;
import com.tce.smart.platform.utils.NumberUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 14:13
 */
@Component
@AllArgsConstructor
public class EleMeterRespWrapper extends BaseWrapper<SmtEleMeter, EleMeterRespDTO> {

	private final SmtEleMeterTagService tagService;
	private final SmtEleMeterConcentratorService concentratorService;
	private final SmtAreaService areaService;

	@Override
	protected EleMeterRespDTO warp(SmtEleMeter model) {
		EleMeterRespDTO dto = BeanUtils.transform(EleMeterRespDTO.class, model);
		dto.setStatus(MeterStatusEnum.desc(model.getIsOnline()));
		// 设备标签
		List<SmtDeviceTag> tagList = tagService.getTagByMeterId(model.getId());
		if (CollUtil.isNotEmpty(tagList)) {
			dto.setTagList(BeanUtils.batchTransform(DeviceTagListDTO.class, tagList));
		}
		// 阀门控制
		dto.setValveStatus(model.getIsOpen());
		SmtEleMeterConcentrator concentrator = concentratorService.getById(model.getConcentratorId());
		if (Objects.nonNull(concentrator)) {
			dto.setConcentratorName(concentrator.getName());
		}
		try {
			String reading = String.valueOf(dto.getRatio() * Double.parseDouble(dto.getCurrentReading()));
			dto.setCurrentReading(NumberUtils.strFormat(reading));
		} catch (Exception e){}

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
