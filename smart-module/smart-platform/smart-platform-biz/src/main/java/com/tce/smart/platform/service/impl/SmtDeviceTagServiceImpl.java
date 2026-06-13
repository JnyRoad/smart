package com.tce.smart.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.platform.core.entity.SmtDeviceTag;
import com.tce.smart.platform.core.entity.SmtDeviceTagRelation;
import com.tce.smart.platform.core.mapper.SmtDeviceTagMapper;
import com.tce.smart.platform.service.SmtDeviceTagRelationService;
import com.tce.smart.platform.service.SmtDeviceTagService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author sunfujian
 * @date 2021/7/29 11:26
 */
@Service
@AllArgsConstructor
public class SmtDeviceTagServiceImpl extends ServiceImpl<SmtDeviceTagMapper, SmtDeviceTag> implements SmtDeviceTagService {

	private final SmtDeviceTagRelationService deviceTagRelationService;

	@Override
	public IPage<SmtDeviceTag> getPage(Page page, String tagName) {
		return page(page, new LambdaQueryWrapper<SmtDeviceTag>()
				.like(StrUtil.isNotBlank(tagName), SmtDeviceTag::getTagName, tagName)
				.orderByDesc(SmtDeviceTag::getCreateTime));
	}

	@Override
	public Boolean save(String tagName) {
		SmtDeviceTag deviceTag = new SmtDeviceTag();
		deviceTag.setTagName(tagName);
		deviceTag.setCreateTime(DateUtils.date());
		return save(deviceTag);
	}

	@Override
	public Boolean update(Long id, String tagName) {
		SmtDeviceTag deviceTag = getById(id);
		if (Objects.isNull(deviceTag)) {
			throw new SmartException("设备标签不存在");
		}
		if (exist(tagName)) {
			throw new SmartException("设备标签名称已存在");
		}
		deviceTag.setTagName(tagName);
		return updateById(deviceTag);
	}

	@Override
	public Boolean exist(String tagName) {
		return count(new LambdaQueryWrapper<SmtDeviceTag>().eq(SmtDeviceTag::getTagName, tagName)) > 0;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeById(Serializable id) {
		deviceTagRelationService.remove(new LambdaQueryWrapper<SmtDeviceTagRelation>()
				.eq(SmtDeviceTagRelation::getTagId, id));
		return super.removeById(id);
	}

	@Override
	public List<SmtDeviceTag> getByDeviceId(String deviceId) {
		return baseMapper.getByDeviceId(deviceId);
	}
}
