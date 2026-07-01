package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtBusinessDeviceAuth;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.mapper.SmtDeviceAuthorityRelationMapper;
import com.tce.smart.platform.core.model.DeviceTree;
import com.tce.smart.platform.service.SmtBusinessDeviceAuthService;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtStaffDeviceAuthService;
import com.tce.smart.tool.enums.DeviceAuthorityEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 设备权限表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:34
 */
@Service
public class SmtDeviceAuthorityRelationServiceImpl extends ServiceImpl<SmtDeviceAuthorityRelationMapper, SmtDeviceAuthorityRelation> implements SmtDeviceAuthorityRelationService {
	@Autowired
	private SmtBusinessDeviceAuthService smtBusinessDeviceAuthService;
	@Autowired
	private SmtStaffDeviceAuthService smtStaffDeviceAuthService;

	@Override
	public List<DeviceTree> getDevice(String areaId, List<Integer> types) {
		return this.baseMapper.getDevice(areaId, types);
	}

	@Override
	public List<DeviceTree> getPark(List<Integer> parkIds) {
		return this.baseMapper.getPark(parkIds);
	}

	@Override
	public List<DeviceTree> getArea(Integer parkId, Integer pId) {
		return this.baseMapper.getArea(parkId, pId);
	}

	@Override
	public List<String> getDeviceIds(Integer authorityId, List<Integer> parkIds) {
		return this.baseMapper.getDeviceIds(authorityId, parkIds);
	}

	public List<SmtDeviceAuthorityRelation> getDevices(Integer authorityId, List<Integer> parkIds) {
		return this.baseMapper.getDevices(authorityId, parkIds);
	}

	@Override
	public List<SmtDeviceAuthorityRelation> getRelation(DeviceAuthorityEnum deviceAuthorityEnum, Integer parkId) {
		return this.list(
				Wrappers.<SmtDeviceAuthorityRelation>query().lambda()
						.eq(SmtDeviceAuthorityRelation::getAuthorityId, deviceAuthorityEnum.getCode())
						.eq(SmtDeviceAuthorityRelation::getParkId, parkId)
		);
	}

	@Override
	public List<SmtDeviceAuthorityRelation> getRelationByAuthId(List<Integer> authId) {
		return this.list(
				Wrappers.<SmtDeviceAuthorityRelation>query().lambda()
						.in(SmtDeviceAuthorityRelation::getAuthorityId, authId)
		);
	}

	@Override
	public List<SmtDeviceAuthorityRelation> getRelationByDeviceId(String deviceId) {
		return this.list(
				Wrappers.<SmtDeviceAuthorityRelation>query().lambda()
						.eq(SmtDeviceAuthorityRelation::getDeviceId, deviceId)
		);
	}

	@Override
	public List<SmtDeviceAuthorityRelation> getRelationAuth(Integer parkId, Integer businessCode, DeviceAuthorityEnum deviceAuthorityEnum) {
		SmtBusinessDeviceAuth auth = smtBusinessDeviceAuthService.getDeviceAuth(parkId, businessCode);
		if (Objects.nonNull(auth)) {
			List<SmtDeviceAuthorityRelation> visitorDeviceList = new ArrayList<>();
			List<SmtDeviceAuthorityRelation> relation = this.baseMapper.selectList(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>().eq(SmtDeviceAuthorityRelation::getAuthorityId,auth.getAuthId()));
			if (CollectionUtil.isNotEmpty(relation)) {
				relation.forEach(ar -> {
					visitorDeviceList.add(ar);
				});
			}
			return visitorDeviceList;
		}

		List<SmtDeviceAuthorityRelation> visitorDeviceList = new ArrayList<>();
		if (null != deviceAuthorityEnum) {
			//自定义权限配置为空，启用默认权限
			visitorDeviceList = this.getRelation(deviceAuthorityEnum, parkId);
		}
		return visitorDeviceList;
	}

	@Override
	public List<SmtDeviceAuthorityRelation> getMulRelationAuth(Long staffId, List<Integer> parkIds, Integer businessCode, DeviceAuthorityEnum deviceAuthorityEnum) {
		// 2021-09-10 修改下发权限读取表
		//List<SmtBusinessDeviceAuth> authList = smtBusinessDeviceAuthService.getMulDeviceAuth(parkIds, businessCode);
		List<SmtStaffDeviceAuth> authList = smtStaffDeviceAuthService.queryList(staffId);
		if (CollectionUtils.isNotEmpty(authList)) {
			List<SmtDeviceAuthorityRelation> visitorDeviceList = new ArrayList<>();
			authList.forEach(auth -> {
				List<SmtDeviceAuthorityRelation> relation = this.baseMapper.selectList(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>().eq(SmtDeviceAuthorityRelation::getAuthorityId,auth.getAuthId()));
				if (CollectionUtil.isNotEmpty(relation)) {
					relation.forEach(ar -> {
						visitorDeviceList.add(ar);
					});
				}
			});
			return visitorDeviceList;
		}

		List<SmtDeviceAuthorityRelation> visitorDeviceList = new ArrayList<>();
		if(null != deviceAuthorityEnum){
			//自定义权限配置为空，启用默认权限
			visitorDeviceList = this.getDevices(businessCode, parkIds);
		}
		return visitorDeviceList;
	}

}
