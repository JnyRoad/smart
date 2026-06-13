package com.tce.smart.platform.service.watermeter.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterTagAddDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTag;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterTag;
import com.tce.smart.platform.core.mapper.watermeter.SmtEleMeterTagMapper;
import com.tce.smart.platform.service.SmtDeviceTagService;
import com.tce.smart.platform.service.watermeter.SmtEleMeterTagService;
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
 * @since: 2021/8/19 10:41
 */
@Slf4j
@Service
public class SmtEleMeterTagServiceImpl extends ServiceImpl<SmtEleMeterTagMapper, SmtEleMeterTag> implements SmtEleMeterTagService {

	@Autowired
	private SmtDeviceTagService deviceTagService;

	@Override
	public List<Long> getMeterIdsByTagIds(List<Long> tagIds) {
		if(CollectionUtils.isNotEmpty(tagIds)) {
			List<SmtEleMeterTag> meterTags = this.list(Wrappers.<SmtEleMeterTag>lambdaQuery()
					.in(SmtEleMeterTag::getTagId, tagIds));
			if(CollectionUtils.isNotEmpty(meterTags)) {
				return meterTags.stream().map(SmtEleMeterTag::getMeterId).collect(Collectors.toList());
			}
		}
		return new ArrayList<>();
	}

	@Override
	public List<SmtDeviceTag> getTagByMeterId(Long meterId) {
		List<SmtEleMeterTag> tags = this.list(Wrappers.<SmtEleMeterTag>lambdaQuery()
				.eq(SmtEleMeterTag::getMeterId, meterId));
		if(CollectionUtils.isNotEmpty(tags)) {
			List<Long> tagIds = tags.stream().map(SmtEleMeterTag::getTagId).collect(Collectors.toList());
			Collection<SmtDeviceTag> collection = deviceTagService.listByIds(tagIds);
			if(CollectionUtils.isNotEmpty(collection)) {
				return new ArrayList<>(collection);
			}
		}
		return null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean setMeterTag(EleMeterTagAddDTO dto) {
		boolean save = true;
		for (Long meterId : dto.getMeterIds()) {
			this.remove(Wrappers.<SmtEleMeterTag>lambdaQuery().eq(SmtEleMeterTag::getMeterId, meterId));
			List<SmtEleMeterTag> meterTags = new ArrayList<>();
			if(CollectionUtils.isNotEmpty(dto.getTagIds())) {
				for (Long tagId : dto.getTagIds()) {
					meterTags.add(SmtEleMeterTag.builder()
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
