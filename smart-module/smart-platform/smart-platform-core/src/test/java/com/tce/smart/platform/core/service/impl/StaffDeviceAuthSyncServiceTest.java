package com.tce.smart.platform.core.service.impl;

import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.mapper.SmtDeviceAuthorityRelationMapper;
import com.tce.smart.platform.core.mapper.SmtIscDownRecordMapper;
import com.tce.smart.platform.core.mapper.SmtStaffDeviceAuthMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.mapper.SmtTaskDownRecordMapper;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

public class StaffDeviceAuthSyncServiceTest {

	@Test
	public void syncAfterDeleteRemovesSingleDeviceAuthorityWhenNoActiveRecordRemains() {
		SmtStaffDeviceAuthMapper staffAuthMapper = Mockito.mock(SmtStaffDeviceAuthMapper.class);
		SmtDeviceAuthorityRelationMapper relationMapper = Mockito.mock(SmtDeviceAuthorityRelationMapper.class);
		SmtTaskDownRecordMapper downRecordMapper = Mockito.mock(SmtTaskDownRecordMapper.class);
		SmtIscDownRecordMapper iscDownRecordMapper = Mockito.mock(SmtIscDownRecordMapper.class);
		StaffDeviceAuthSyncService service = newService(staffAuthMapper, relationMapper,
				downRecordMapper, iscDownRecordMapper, Mockito.mock(SmtStaffMapper.class));

		Mockito.when(staffAuthMapper.selectList(Mockito.any()))
				.thenReturn(Collections.singletonList(staffAuth(10, 1001L, 100)));
		Mockito.when(relationMapper.selectList(Mockito.any()))
				.thenReturn(Collections.singletonList(relation(100, "device-A")))
				.thenReturn(Collections.singletonList(relation(100, "device-A")));
		Mockito.when(downRecordMapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(iscDownRecordMapper.selectCount(Mockito.any())).thenReturn(0);

		service.syncAfterDelete("device-A", "1001", "8031249-李世勋",
				DeviceTaskConstants.CARD, DeviceTaskConstants.CARD_STAFF_IMPORT);

		Mockito.verify(staffAuthMapper).deleteById(10);
	}

	@Test
	public void syncAfterDeleteKeepsMultiDeviceAuthorityWhenAnotherDeviceStillHasPermission() {
		SmtStaffDeviceAuthMapper staffAuthMapper = Mockito.mock(SmtStaffDeviceAuthMapper.class);
		SmtDeviceAuthorityRelationMapper relationMapper = Mockito.mock(SmtDeviceAuthorityRelationMapper.class);
		SmtTaskDownRecordMapper downRecordMapper = Mockito.mock(SmtTaskDownRecordMapper.class);
		SmtIscDownRecordMapper iscDownRecordMapper = Mockito.mock(SmtIscDownRecordMapper.class);
		StaffDeviceAuthSyncService service = newService(staffAuthMapper, relationMapper,
				downRecordMapper, iscDownRecordMapper, Mockito.mock(SmtStaffMapper.class));

		Mockito.when(staffAuthMapper.selectList(Mockito.any()))
				.thenReturn(Collections.singletonList(staffAuth(10, 1001L, 100)));
		Mockito.when(relationMapper.selectList(Mockito.any()))
				.thenReturn(Collections.singletonList(relation(100, "device-A")))
				.thenReturn(Arrays.asList(relation(100, "device-A"), relation(100, "device-B")));
		Mockito.when(downRecordMapper.selectCount(Mockito.any())).thenReturn(1);

		service.syncAfterDelete("device-A", "1001", "8031249-李世勋",
				DeviceTaskConstants.CARD, DeviceTaskConstants.CARD_STAFF_IMPORT);

		Mockito.verify(staffAuthMapper, Mockito.never()).deleteById(Mockito.anyInt());
	}

	@Test
	public void syncAfterDeleteRemovesMultiDeviceAuthorityAfterLastDevicePermissionGone() {
		SmtStaffDeviceAuthMapper staffAuthMapper = Mockito.mock(SmtStaffDeviceAuthMapper.class);
		SmtDeviceAuthorityRelationMapper relationMapper = Mockito.mock(SmtDeviceAuthorityRelationMapper.class);
		SmtTaskDownRecordMapper downRecordMapper = Mockito.mock(SmtTaskDownRecordMapper.class);
		SmtIscDownRecordMapper iscDownRecordMapper = Mockito.mock(SmtIscDownRecordMapper.class);
		StaffDeviceAuthSyncService service = newService(staffAuthMapper, relationMapper,
				downRecordMapper, iscDownRecordMapper, Mockito.mock(SmtStaffMapper.class));

		Mockito.when(staffAuthMapper.selectList(Mockito.any()))
				.thenReturn(Collections.singletonList(staffAuth(10, 1001L, 100)));
		Mockito.when(relationMapper.selectList(Mockito.any()))
				.thenReturn(Collections.singletonList(relation(100, "device-B")))
				.thenReturn(Arrays.asList(relation(100, "device-A"), relation(100, "device-B")));
		Mockito.when(downRecordMapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(iscDownRecordMapper.selectCount(Mockito.any())).thenReturn(0);

		service.syncAfterDelete("device-B", "1001", "8031249-李世勋",
				DeviceTaskConstants.CARD, DeviceTaskConstants.CARD_STAFF_IMPORT);

		Mockito.verify(staffAuthMapper).deleteById(10);
	}

	@Test
	public void syncAfterDeleteFallsBackToBadgeWhenCardNoIsNotStaffId() {
		SmtStaffDeviceAuthMapper staffAuthMapper = Mockito.mock(SmtStaffDeviceAuthMapper.class);
		SmtDeviceAuthorityRelationMapper relationMapper = Mockito.mock(SmtDeviceAuthorityRelationMapper.class);
		SmtTaskDownRecordMapper downRecordMapper = Mockito.mock(SmtTaskDownRecordMapper.class);
		SmtIscDownRecordMapper iscDownRecordMapper = Mockito.mock(SmtIscDownRecordMapper.class);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		StaffDeviceAuthSyncService service = newService(staffAuthMapper, relationMapper,
				downRecordMapper, iscDownRecordMapper, staffMapper);

		Mockito.when(staffAuthMapper.selectList(Mockito.any()))
				.thenReturn(Collections.emptyList())
				.thenReturn(Collections.singletonList(staffAuth(10, 1001L, 100)));
		SmtStaff staff = new SmtStaff();
		staff.setId(1001L);
		Mockito.when(staffMapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(staff));
		Mockito.when(relationMapper.selectList(Mockito.any()))
				.thenReturn(Collections.singletonList(relation(100, "device-A")))
				.thenReturn(Collections.singletonList(relation(100, "device-A")));
		Mockito.when(downRecordMapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(iscDownRecordMapper.selectCount(Mockito.any())).thenReturn(0);

		service.syncAfterDelete("device-A", "8031249", "8031249-李世勋",
				DeviceTaskConstants.CARD, DeviceTaskConstants.CARD_STAFF_IMPORT);

		Mockito.verify(staffAuthMapper).deleteById(10);
	}

	private StaffDeviceAuthSyncService newService(SmtStaffDeviceAuthMapper staffAuthMapper,
												  SmtDeviceAuthorityRelationMapper relationMapper,
												  SmtTaskDownRecordMapper downRecordMapper,
												  SmtIscDownRecordMapper iscDownRecordMapper,
												  SmtStaffMapper staffMapper) {
		return new StaffDeviceAuthSyncService(staffAuthMapper, relationMapper, downRecordMapper,
				iscDownRecordMapper, staffMapper);
	}

	private SmtStaffDeviceAuth staffAuth(Integer id, Long staffId, Integer authId) {
		SmtStaffDeviceAuth staffAuth = new SmtStaffDeviceAuth();
		staffAuth.setId(id);
		staffAuth.setStaffId(staffId);
		staffAuth.setAuthId(authId);
		return staffAuth;
	}

	private SmtDeviceAuthorityRelation relation(Integer authId, String deviceId) {
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setAuthorityId(authId);
		relation.setDeviceId(deviceId);
		return relation;
	}
}
