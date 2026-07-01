package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtBusinessDeviceAuth;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.entity.SmtVehicleApply;
import com.tce.smart.platform.core.model.DeviceDecommissionPlan;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.service.DeviceDecommissionService;
import com.tce.smart.platform.service.SmtBusinessDeviceAuthService;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtDeviceAuthorityService;
import com.tce.smart.platform.service.SmtStaffDeviceAuthService;
import com.tce.smart.platform.service.SmtVehicleApplyService;
import com.tce.smart.tool.enums.DeviceAuthorityEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 设备下线编排服务：plan() 只读计算，execute()/decommissionDevice() 按计算结果执行清理。
 */
@Service
@AllArgsConstructor
public class DeviceDecommissionServiceImpl implements DeviceDecommissionService {

	private final SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;

	private final SmtDeviceAuthorityService smtDeviceAuthorityService;

	private final SmtStaffDeviceAuthService smtStaffDeviceAuthService;

	private final SmtVehicleApplyService smtVehicleApplyService;

	private final SmtBusinessDeviceAuthService smtBusinessDeviceAuthService;

	private final SmtDeviceService smtDeviceService;

	@Override
	public DeviceDecommissionPlan plan(String deviceId) {
		DeviceDecommissionPlan resultPlan = new DeviceDecommissionPlan();
		resultPlan.setDeviceId(deviceId);

		List<SmtDeviceAuthorityRelation> ownRelations = smtDeviceAuthorityRelationService.getRelationByDeviceId(deviceId);
		if (CollUtil.isEmpty(ownRelations)) {
			return resultPlan;
		}

		List<Integer> authorityIds = ownRelations.stream()
				.map(SmtDeviceAuthorityRelation::getAuthorityId)
				.distinct()
				.collect(Collectors.toList());

		// listByIds 返回 Collection，这里按仓库惯例转成 List 便于后续遍历
		List<SmtDeviceAuthority> authorities = new ArrayList<>(smtDeviceAuthorityService.listByIds(authorityIds));

		Map<Integer, Long> deviceCountByAuthorityId = smtDeviceAuthorityRelationService.getRelationByAuthId(authorityIds).stream()
				.collect(Collectors.groupingBy(SmtDeviceAuthorityRelation::getAuthorityId, Collectors.counting()));

		Map<Integer, Long> staffCountByAuthorityId = smtStaffDeviceAuthService.list(
						Wrappers.<SmtStaffDeviceAuth>lambdaQuery().in(SmtStaffDeviceAuth::getAuthId, authorityIds)).stream()
				.collect(Collectors.groupingBy(SmtStaffDeviceAuth::getAuthId, Collectors.counting()));

		Map<Integer, Long> vehicleCountByAuthorityId = smtVehicleApplyService.list(
						Wrappers.<SmtVehicleApply>lambdaQuery().in(SmtVehicleApply::getAuthorityId, authorityIds)).stream()
				.collect(Collectors.groupingBy(SmtVehicleApply::getAuthorityId, Collectors.counting()));

		Set<Integer> businessDefaultAuthorityIds = smtBusinessDeviceAuthService.list(
						Wrappers.<SmtBusinessDeviceAuth>lambdaQuery().in(SmtBusinessDeviceAuth::getAuthId, authorityIds)).stream()
				.map(SmtBusinessDeviceAuth::getAuthId)
				.collect(Collectors.toSet());

		List<DeviceDecommissionPlan.AffectedAuthority> affectedList = new ArrayList<>();
		for (SmtDeviceAuthority authority : authorities) {
			long totalDeviceCount = deviceCountByAuthorityId.getOrDefault(authority.getId(), 0L);
			int remainingDeviceCount = (int) Math.max(0, totalDeviceCount - 1);
			boolean protectedAuthority = businessDefaultAuthorityIds.contains(authority.getId())
					|| DeviceAuthorityEnum.existAuthority(authority.getId());
			boolean willCascadeDelete = remainingDeviceCount == 0 && !protectedAuthority;

			DeviceDecommissionPlan.AffectedAuthority affected = new DeviceDecommissionPlan.AffectedAuthority();
			affected.setAuthorityId(authority.getId());
			affected.setAuthorityName(authority.getAuthorityName());
			affected.setRemainingDeviceCount(remainingDeviceCount);
			affected.setStaffCount(staffCountByAuthorityId.getOrDefault(authority.getId(), 0L).intValue());
			affected.setVehicleCount(vehicleCountByAuthorityId.getOrDefault(authority.getId(), 0L).intValue());
			affected.setProtectedAuthority(protectedAuthority);
			affected.setWillCascadeDelete(willCascadeDelete);
			affectedList.add(affected);
		}
		resultPlan.setAffectedAuthorities(affectedList);
		return resultPlan;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void execute(DeviceDecommissionPlan plan) {
		if (plan == null || CollUtil.isEmpty(plan.getAffectedAuthorities())) {
			return;
		}
		String deviceId = plan.getDeviceId();
		for (DeviceDecommissionPlan.AffectedAuthority affected : plan.getAffectedAuthorities()) {
			smtDeviceAuthorityService.revokeDeviceAccess(affected.getAuthorityId(), deviceId);
			smtDeviceAuthorityRelationService.remove(
					Wrappers.<SmtDeviceAuthorityRelation>lambdaQuery()
							.eq(SmtDeviceAuthorityRelation::getAuthorityId, affected.getAuthorityId())
							.eq(SmtDeviceAuthorityRelation::getDeviceId, deviceId));
			if (affected.isWillCascadeDelete()) {
				smtStaffDeviceAuthService.removeByAuthId(affected.getAuthorityId());
				smtVehicleApplyService.removeByAuthId(affected.getAuthorityId());
				smtDeviceAuthorityService.removeById(affected.getAuthorityId());
			}
		}
	}

	@Override
	public Result decommissionDevice(String deviceId) {
		throw new UnsupportedOperationException("implemented in Task 5");
	}
}
