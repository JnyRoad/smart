package com.tce.smart.platform.service.impl;

import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.mapper.SmtDeviceAuthorityMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtVehicleMapper;
import com.tce.smart.platform.core.service.SmtBatchDeviceTaskService;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.service.impl.SmtIscDeviceTaskServiceImpl;
import com.tce.smart.platform.service.SmtBusinessDeviceAuthService;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtStaffDeviceAuthService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.SmtVehicleApplyService;
import com.tce.smart.tool.enums.DeviceAuthTypeEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SmtDeviceAuthorityServiceImplTest {

	@Test
	public void deviceAuthRelationClearKeepsDeviceScopeIndependentPerStaff() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService, staffAuthService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setType(DeviceAuthTypeEnum.PERSON.getCode());
		Mockito.when(authorityMapper.selectById(100)).thenReturn(authority);

		SmtDeviceAuthorityRelation targetDevice = relation(100, "device-A");
		SmtDeviceAuthorityRelation otherAuthSameDevice = relation(200, "device-A");
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenAnswer(invocation -> {
			List<Integer> authIds = invocation.getArgument(0);
			if (authIds.contains(100)) {
				return Collections.singletonList(targetDevice);
			}
			if (authIds.contains(200)) {
				return Collections.singletonList(otherAuthSameDevice);
			}
			return Collections.emptyList();
		});

		SmtStaffDeviceAuth firstStaffTargetAuth = staffAuth(1, 1001L, 100);
		SmtStaffDeviceAuth secondStaffTargetAuth = staffAuth(2, 1002L, 100);
		SmtStaffDeviceAuth firstStaffOtherAuth = staffAuth(3, 1001L, 200);
		Mockito.when(staffAuthService.list(Mockito.any()))
				.thenReturn(Arrays.asList(firstStaffTargetAuth, secondStaffTargetAuth))
				.thenReturn(Collections.singletonList(firstStaffOtherAuth))
				.thenReturn(Collections.emptyList());
		Mockito.when(staffAuthService.removeAuthToDevice(Mockito.any(SmtStaffDeviceAuth.class), Mockito.anyList()))
				.thenReturn(true);

		service.deviceAuthRelationClear(100);

		ArgumentCaptor<SmtStaffDeviceAuth> staffAuthCaptor = ArgumentCaptor.forClass(SmtStaffDeviceAuth.class);
		ArgumentCaptor<List> deviceIdsCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(staffAuthService, Mockito.times(2))
				.removeAuthToDevice(staffAuthCaptor.capture(), deviceIdsCaptor.capture());

		Assert.assertEquals(firstStaffTargetAuth, staffAuthCaptor.getAllValues().get(0));
		Assert.assertTrue(deviceIdsCaptor.getAllValues().get(0).isEmpty());
		Assert.assertEquals(secondStaffTargetAuth, staffAuthCaptor.getAllValues().get(1));
		Assert.assertEquals(Collections.singletonList("device-A"), deviceIdsCaptor.getAllValues().get(1));
	}

	private SmtDeviceAuthorityServiceImpl newService(SmtDeviceAuthorityMapper authorityMapper,
													 SmtDeviceAuthorityRelationService relationService,
													 SmtStaffDeviceAuthService staffAuthService) {
		return new SmtDeviceAuthorityServiceImpl(
				Mockito.mock(SmtDeviceService.class),
				authorityMapper,
				relationService,
				Mockito.mock(SmtBusinessDeviceAuthService.class),
				staffAuthService,
				Mockito.mock(SmtDeviceMapper.class),
				Mockito.mock(SmtDeviceTaskService.class),
				Mockito.mock(SmtIscDeviceTaskService.class),
				Mockito.mock(SmtVehicleApplyService.class),
				Mockito.mock(SmtStaffService.class),
				Mockito.mock(SmtVehicleMapper.class),
				Mockito.mock(SmtIscDeviceTaskServiceImpl.class),
				Mockito.mock(SmtBatchDeviceTaskService.class));
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

	private void setField(Object target, String name, Object value) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				field.set(target, value);
				return;
			} catch (NoSuchFieldException ignored) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}
}
