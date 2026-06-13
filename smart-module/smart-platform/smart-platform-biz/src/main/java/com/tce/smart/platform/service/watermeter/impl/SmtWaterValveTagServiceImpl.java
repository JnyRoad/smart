package com.tce.smart.platform.service.watermeter.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.platform.api.dto.req.watermeter.WaterValveTagAddDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTag;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterValveTag;
import com.tce.smart.platform.core.mapper.watermeter.SmtWaterValveTagMapper;
import com.tce.smart.platform.service.SmtDeviceTagService;
import com.tce.smart.platform.service.watermeter.SmtWaterValveTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:42
 */
@Slf4j
@Service
public class SmtWaterValveTagServiceImpl extends ServiceImpl<SmtWaterValveTagMapper, SmtWaterValveTag> implements SmtWaterValveTagService {

	@Autowired
	private SmtDeviceTagService deviceTagService;

	@Override
	public List<SmtDeviceTag> getTagByValveId(Long valveId) {
		List<SmtWaterValveTag> tags = this.list(Wrappers.<SmtWaterValveTag>lambdaQuery()
				.eq(SmtWaterValveTag::getValveId, valveId));
		if(CollectionUtils.isNotEmpty(tags)) {
			List<Long> tagIds = tags.stream().map(SmtWaterValveTag::getTagId).collect(Collectors.toList());
			Collection<SmtDeviceTag> collection = deviceTagService.listByIds(tagIds);
			if(CollectionUtils.isNotEmpty(collection)) {
				return new ArrayList<>(collection);
			}
		}
		return null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean setValveTag(WaterValveTagAddDTO dto) {
		boolean save = true;
		for (Long valveId : dto.getValveIds()) {
			this.remove(Wrappers.<SmtWaterValveTag>lambdaQuery().eq(SmtWaterValveTag::getValveId, valveId));
			List<SmtWaterValveTag> meterTags = new ArrayList<>();
			if(CollectionUtils.isNotEmpty(dto.getTagIds())) {
				for (Long tagId : dto.getTagIds()) {
					meterTags.add(SmtWaterValveTag.builder()
							.valveId(valveId)
							.tagId(tagId)
							.build());
				}
				save = this.saveBatch(meterTags);
			}
		}
		return save;
	}
}
