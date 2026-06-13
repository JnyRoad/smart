package com.tce.smart.platform.service.watermeter.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterTagAddDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTag;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterTag;
import com.tce.smart.platform.core.mapper.watermeter.SmtWaterMeterTagMapper;
import com.tce.smart.platform.service.SmtDeviceTagService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterTagService;
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
public class SmtWaterMeterTagServiceImpl extends ServiceImpl<SmtWaterMeterTagMapper, SmtWaterMeterTag> implements SmtWaterMeterTagService {

	@Autowired
	private SmtDeviceTagService deviceTagService;

	@Override
	public List<Long> getMeterIdsByTagIds(List<Long> tagIds) {
		if(CollectionUtils.isNotEmpty(tagIds)) {
			List<SmtWaterMeterTag> meterTags = this.list(Wrappers.<SmtWaterMeterTag>lambdaQuery()
					.in(SmtWaterMeterTag::getTagId, tagIds));
			if(CollectionUtils.isNotEmpty(meterTags)) {
				return meterTags.stream().map(SmtWaterMeterTag::getMeterId).collect(Collectors.toList());
			}
		}
		return new ArrayList<>();
	}

	@Override
	public List<SmtDeviceTag> getTagByMeterId(Long meterId) {
		List<SmtWaterMeterTag> tags = this.list(Wrappers.<SmtWaterMeterTag>lambdaQuery()
				.eq(SmtWaterMeterTag::getMeterId, meterId));
		if(CollectionUtils.isNotEmpty(tags)) {
			List<Long> tagIds = tags.stream().map(SmtWaterMeterTag::getTagId).collect(Collectors.toList());
			Collection<SmtDeviceTag> collection = deviceTagService.listByIds(tagIds);
			if(CollectionUtils.isNotEmpty(collection)) {
				return new ArrayList<>(collection);
			}
		}
		return null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean setMeterTag(WaterMeterTagAddDTO dto) {
		boolean save = true;
		for (Long meterId : dto.getMeterIds()) {
			this.remove(Wrappers.<SmtWaterMeterTag>lambdaQuery().eq(SmtWaterMeterTag::getMeterId, meterId));
			List<SmtWaterMeterTag> meterTags = new ArrayList<>();
			if(CollectionUtils.isNotEmpty(dto.getTagIds())) {
				for (Long tagId : dto.getTagIds()) {
					meterTags.add(SmtWaterMeterTag.builder()
							.meterId(meterId)
							.tagId(tagId)
							.build());
				}
				save = this.saveBatch(meterTags);
			}
		}
		return save;
	}
}
