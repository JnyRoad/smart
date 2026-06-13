package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.entity.SmtTaskDownRecord;
import com.tce.smart.platform.core.mapper.SmtDeviceAuthorityRelationMapper;
import com.tce.smart.platform.core.mapper.SmtIscDownRecordMapper;
import com.tce.smart.platform.core.mapper.SmtStaffDeviceAuthMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.mapper.SmtTaskDownRecordMapper;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Keeps staff permission-group relations consistent with actual device down records.
 */
@Service
@Slf4j
@AllArgsConstructor
public class StaffDeviceAuthSyncService {

	private static final List<Integer> STAFF_FACE_SERVICE_TYPES = Arrays.asList(
			DeviceTaskConstants.CARD_STAFF_IMPORT, DeviceTaskConstants.UPDATE_FACE);

	private final SmtStaffDeviceAuthMapper staffDeviceAuthMapper;

	private final SmtDeviceAuthorityRelationMapper deviceAuthorityRelationMapper;

	private final SmtTaskDownRecordMapper taskDownRecordMapper;

	private final SmtIscDownRecordMapper iscDownRecordMapper;

	private final SmtStaffMapper staffMapper;

	public void syncAfterDelete(SmtTaskDownRecord record) {
		if (record == null) {
			return;
		}
		syncAfterDelete(record.getDeviceCode(), record.getCardNo(), record.getGeneral(),
				record.getDeviceType(), record.getServiceType());
	}

	public void syncAfterDelete(SmtIscDownRecord record) {
		if (record == null) {
			return;
		}
		syncAfterDelete(record.getDeviceCode(), record.getCardNo(), record.getGeneral(),
				record.getDeviceType(), record.getServiceType());
	}

	public void syncAfterDelete(String deviceCode, String cardNo, String general,
								Integer deviceType, Integer serviceType) {
		if (!isStaffFaceAccess(deviceType, serviceType) || StrUtil.isBlank(deviceCode) || StrUtil.isBlank(cardNo)) {
			return;
		}
		StaffAuthContext context = findStaffAuthContext(cardNo, general);
		if (context == null || CollectionUtil.isEmpty(context.staffAuths)) {
			return;
		}
		List<Integer> authIds = context.staffAuths.stream()
				.map(SmtStaffDeviceAuth::getAuthId)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
		if (CollectionUtil.isEmpty(authIds)) {
			return;
		}
		Set<Integer> affectedAuthIds = deviceAuthorityRelationMapper.selectList(
						Wrappers.<SmtDeviceAuthorityRelation>lambdaQuery()
								.in(SmtDeviceAuthorityRelation::getAuthorityId, authIds)
								.eq(SmtDeviceAuthorityRelation::getDeviceId, deviceCode))
				.stream()
				.map(SmtDeviceAuthorityRelation::getAuthorityId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		if (CollectionUtil.isEmpty(affectedAuthIds)) {
			return;
		}
		for (SmtStaffDeviceAuth staffAuth : context.staffAuths) {
			if (staffAuth.getId() == null || !affectedAuthIds.contains(staffAuth.getAuthId())) {
				continue;
			}
			List<String> authorityDeviceIds = getAuthorityDeviceIds(staffAuth.getAuthId());
			if (CollectionUtil.isEmpty(authorityDeviceIds)) {
				continue;
			}
			if (!hasAnyActiveDownRecord(String.valueOf(context.staffId), authorityDeviceIds)) {
				staffDeviceAuthMapper.deleteById(staffAuth.getId());
				log.info("设备权限删除后同步移除人员权限组关系，staffId={}, authId={}, deviceCode={}",
						context.staffId, staffAuth.getAuthId(), deviceCode);
			}
		}
	}

	private boolean isStaffFaceAccess(Integer deviceType, Integer serviceType) {
		return DeviceTaskConstants.CARD.equals(deviceType) && STAFF_FACE_SERVICE_TYPES.contains(serviceType);
	}

	private StaffAuthContext findStaffAuthContext(String cardNo, String general) {
		Long staffId = parseLong(cardNo);
		List<SmtStaffDeviceAuth> staffAuths = Collections.emptyList();
		if (staffId != null) {
			staffAuths = getStaffAuths(staffId);
		}
		if (CollectionUtil.isNotEmpty(staffAuths)) {
			return new StaffAuthContext(staffId, staffAuths);
		}
		Long staffIdByBadge = findStaffIdByBadge(cardNo);
		if (staffIdByBadge == null) {
			staffIdByBadge = findStaffIdByBadge(parseBadge(general));
		}
		if (staffIdByBadge == null || Objects.equals(staffIdByBadge, staffId)) {
			return null;
		}
		staffAuths = getStaffAuths(staffIdByBadge);
		return new StaffAuthContext(staffIdByBadge, staffAuths);
	}

	private List<SmtStaffDeviceAuth> getStaffAuths(Long staffId) {
		return staffDeviceAuthMapper.selectList(Wrappers.<SmtStaffDeviceAuth>lambdaQuery()
				.eq(SmtStaffDeviceAuth::getStaffId, staffId));
	}

	private List<String> getAuthorityDeviceIds(Integer authId) {
		return deviceAuthorityRelationMapper.selectList(Wrappers.<SmtDeviceAuthorityRelation>lambdaQuery()
						.eq(SmtDeviceAuthorityRelation::getAuthorityId, authId))
				.stream()
				.map(SmtDeviceAuthorityRelation::getDeviceId)
				.filter(StrUtil::isNotBlank)
				.distinct()
				.collect(Collectors.toList());
	}

	private boolean hasAnyActiveDownRecord(String cardNo, List<String> deviceIds) {
		Integer standardCount = taskDownRecordMapper.selectCount(staffFaceDownRecordQuery(cardNo, deviceIds));
		if (standardCount != null && standardCount > 0) {
			return true;
		}
		Integer iscCount = iscDownRecordMapper.selectCount(staffFaceIscDownRecordQuery(cardNo, deviceIds));
		return iscCount != null && iscCount > 0;
	}

	private LambdaQueryWrapper<SmtTaskDownRecord> staffFaceDownRecordQuery(String cardNo, List<String> deviceIds) {
		return Wrappers.<SmtTaskDownRecord>lambdaQuery()
				.eq(SmtTaskDownRecord::getCardNo, cardNo)
				.eq(SmtTaskDownRecord::getDeviceType, DeviceTaskConstants.CARD)
				.in(SmtTaskDownRecord::getServiceType, STAFF_FACE_SERVICE_TYPES)
				.in(SmtTaskDownRecord::getDeviceCode, deviceIds);
	}

	private LambdaQueryWrapper<SmtIscDownRecord> staffFaceIscDownRecordQuery(String cardNo, List<String> deviceIds) {
		return Wrappers.<SmtIscDownRecord>lambdaQuery()
				.eq(SmtIscDownRecord::getCardNo, cardNo)
				.eq(SmtIscDownRecord::getDeviceType, DeviceTaskConstants.CARD)
				.in(SmtIscDownRecord::getServiceType, STAFF_FACE_SERVICE_TYPES)
				.in(SmtIscDownRecord::getDeviceCode, deviceIds);
	}

	private Long findStaffIdByBadge(String badge) {
		if (StrUtil.isBlank(badge)) {
			return null;
		}
		List<SmtStaff> staffList = staffMapper.selectList(Wrappers.<SmtStaff>lambdaQuery()
				.eq(SmtStaff::getBadge, badge));
		if (CollectionUtil.isEmpty(staffList)) {
			return null;
		}
		return staffList.get(0).getId();
	}

	private String parseBadge(String general) {
		if (StrUtil.isBlank(general)) {
			return null;
		}
		int splitIndex = general.indexOf("-");
		if (splitIndex <= 0) {
			return null;
		}
		return general.substring(0, splitIndex);
	}

	private Long parseLong(String value) {
		if (StrUtil.isBlank(value)) {
			return null;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException ignored) {
			return null;
		}
	}

	private static class StaffAuthContext {
		private final Long staffId;

		private final List<SmtStaffDeviceAuth> staffAuths;

		private StaffAuthContext(Long staffId, List<SmtStaffDeviceAuth> staffAuths) {
			this.staffId = staffId;
			this.staffAuths = staffAuths;
		}
	}
}
