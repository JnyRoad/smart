package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.api.dto.req.DeviceTagSetReqDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTagRelation;
import com.tce.smart.platform.core.mapper.SmtDeviceTagRelationMapper;
import com.tce.smart.platform.service.SmtDeviceTagRelationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author sunfujian
 * @date 2021/7/29 11:27
 */
@Service
public class SmtDeviceTagRelationServiceImpl extends ServiceImpl<SmtDeviceTagRelationMapper, SmtDeviceTagRelation> implements SmtDeviceTagRelationService {

	@Override
	public Boolean saveBatch(DeviceTagSetReqDTO tagSetReqDTO) {
		List<String> deviceIds = tagSetReqDTO.getDeviceIds();
		List<Long> tagIds = tagSetReqDTO.getTagIds();
		for (String deviceId : deviceIds) {
			for (Long tagId : tagIds) {
				if (exist(deviceId, tagId)) {
					continue;
				}
				SmtDeviceTagRelation relation = new SmtDeviceTagRelation();
				relation.setDeviceId(deviceId);
				relation.setTagId(tagId);
				save(relation);
			}
		}
		return true;
	}

	@Override
	public Boolean exist(String deviceId, Long tagId) {
		return count(new LambdaQueryWrapper<SmtDeviceTagRelation>()
				.eq(SmtDeviceTagRelation::getDeviceId, deviceId)
				.eq(SmtDeviceTagRelation::getTagId, tagId)) > 0;
	}
}
