package com.tce.smart.platform.service.impl;

import com.tce.smart.platform.core.entity.SmtBusinessDeviceAuth;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.entity.SmtVehicleApply;
import com.tce.smart.platform.core.model.DeviceDecommissionPlan;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.service.SmtBusinessDeviceAuthService;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtDeviceAuthorityService;
import com.tce.smart.platform.service.SmtStaffDeviceAuthService;
import com.tce.smart.platform.service.SmtVehicleApplyService;
import com.tce.smart.tool.enums.DeviceAuthTypeEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeviceDecommissionServiceImplTest {

	@Test
	public void planReturnsEmptyWhenDeviceHasNoAuthorityBinding() {
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		Mockito.when(relationService.getRelationByDeviceId("device-X")).thenReturn(Collections.emptyList());
		DeviceDecommissionServiceImpl service = newService(relationService,
				Mockito.mock(SmtDeviceAuthorityService.class),
				Mockito.mock(SmtStaffDeviceAuthService.class),
				Mockito.mock(SmtVehicleApplyService.class),
				Mockito.mock(SmtBusinessDeviceAuthService.class));

		DeviceDecommissionPlan plan = service.plan("device-X");

		Assert.assertEquals("device-X", plan.getDeviceId());
		Assert.assertTrue(plan.getAffectedAuthorities().isEmpty());
	}

	@Test
	public void planMarksCascadeDeleteWhenDeviceIsTheOnlyOneInAuthority() {
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		Mockito.when(relationService.getRelationByDeviceId("device-A"))
				.thenReturn(Collections.singletonList(relation(100, "device-A")));
		Mockito.when(relationService.getRelationByAuthId(Collections.singletonList(100)))
				.thenReturn(Collections.singletonList(relation(100, "device-A")));

		SmtDeviceAuthorityService authorityService = Mockito.mock(SmtDeviceAuthorityService.class);
		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setAuthorityName("一号门禁权限组");
		authority.setType(DeviceAuthTypeEnum.PERSON.getCode());
		Mockito.when(authorityService.listByIds(Collections.singletonList(100)))
				.thenReturn(Collections.singletonList(authority));

		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		Mockito.when(staffAuthService.list(Mockito.any()))
				.thenReturn(Collections.singletonList(staffAuth(1, 1001L, 100)));

		SmtVehicleApplyService vehicleApplyService = Mockito.mock(SmtVehicleApplyService.class);
		Mockito.when(vehicleApplyService.list(Mockito.any())).thenReturn(Collections.emptyList());

		SmtBusinessDeviceAuthService businessDeviceAuthService = Mockito.mock(SmtBusinessDeviceAuthService.class);
		Mockito.when(businessDeviceAuthService.list(Mockito.any())).thenReturn(Collections.emptyList());

		DeviceDecommissionServiceImpl service = newService(relationService, authorityService,
				staffAuthService, vehicleApplyService, businessDeviceAuthService);

		DeviceDecommissionPlan plan = service.plan("device-A");

		Assert.assertEquals(1, plan.getAffectedAuthorities().size());
		DeviceDecommissionPlan.AffectedAuthority affected = plan.getAffectedAuthorities().get(0);
		Assert.assertEquals(Integer.valueOf(100), affected.getAuthorityId());
		Assert.assertEquals("一号门禁权限组", affected.getAuthorityName());
		Assert.assertEquals(Integer.valueOf(0), affected.getRemainingDeviceCount());
		Assert.assertEquals(Integer.valueOf(1), affected.getStaffCount());
		Assert.assertEquals(Integer.valueOf(0), affected.getVehicleCount());
		Assert.assertFalse(affected.isProtectedAuthority());
		Assert.assertTrue(affected.isWillCascadeDelete());
	}

	@Test
	public void planKeepsAuthorityAliveWhenOtherDevicesRemain() {
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		Mockito.when(relationService.getRelationByDeviceId("device-A"))
				.thenReturn(Collections.singletonList(relation(100, "device-A")));
		Mockito.when(relationService.getRelationByAuthId(Collections.singletonList(100)))
				.thenReturn(Arrays.asList(relation(100, "device-A"), relation(100, "device-B")));

		SmtDeviceAuthorityService authorityService = Mockito.mock(SmtDeviceAuthorityService.class);
		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setAuthorityName("多设备权限组");
		authority.setType(DeviceAuthTypeEnum.PERSON.getCode());
		Mockito.when(authorityService.listByIds(Collections.singletonList(100)))
				.thenReturn(Collections.singletonList(authority));

		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		Mockito.when(staffAuthService.list(Mockito.any())).thenReturn(Collections.emptyList());
		SmtVehicleApplyService vehicleApplyService = Mockito.mock(SmtVehicleApplyService.class);
		Mockito.when(vehicleApplyService.list(Mockito.any())).thenReturn(Collections.emptyList());
		SmtBusinessDeviceAuthService businessDeviceAuthService = Mockito.mock(SmtBusinessDeviceAuthService.class);
		Mockito.when(businessDeviceAuthService.list(Mockito.any())).thenReturn(Collections.emptyList());

		DeviceDecommissionServiceImpl service = newService(relationService, authorityService,
				staffAuthService, vehicleApplyService, businessDeviceAuthService);

		DeviceDecommissionPlan plan = service.plan("device-A");

		DeviceDecommissionPlan.AffectedAuthority affected = plan.getAffectedAuthorities().get(0);
		Assert.assertEquals(Integer.valueOf(1), affected.getRemainingDeviceCount());
		Assert.assertFalse(affected.isWillCascadeDelete());
	}

	@Test
	public void planProtectsAreaDefaultAuthorityFromCascadeDelete() {
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		Mockito.when(relationService.getRelationByDeviceId("device-A"))
				.thenReturn(Collections.singletonList(relation(100, "device-A")));
		Mockito.when(relationService.getRelationByAuthId(Collections.singletonList(100)))
				.thenReturn(Collections.singletonList(relation(100, "device-A")));

		SmtDeviceAuthorityService authorityService = Mockito.mock(SmtDeviceAuthorityService.class);
		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setAuthorityName("区域默认权限组");
		authority.setType(DeviceAuthTypeEnum.PERSON.getCode());
		Mockito.when(authorityService.listByIds(Collections.singletonList(100)))
				.thenReturn(Collections.singletonList(authority));

		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		Mockito.when(staffAuthService.list(Mockito.any())).thenReturn(Collections.emptyList());
		SmtVehicleApplyService vehicleApplyService = Mockito.mock(SmtVehicleApplyService.class);
		Mockito.when(vehicleApplyService.list(Mockito.any())).thenReturn(Collections.emptyList());

		SmtBusinessDeviceAuthService businessDeviceAuthService = Mockito.mock(SmtBusinessDeviceAuthService.class);
		SmtBusinessDeviceAuth businessDeviceAuth = new SmtBusinessDeviceAuth();
		businessDeviceAuth.setAuthId(100);
		Mockito.when(businessDeviceAuthService.list(Mockito.any()))
				.thenReturn(Collections.singletonList(businessDeviceAuth));

		DeviceDecommissionServiceImpl service = newService(relationService, authorityService,
				staffAuthService, vehicleApplyService, businessDeviceAuthService);

		DeviceDecommissionPlan plan = service.plan("device-A");

		DeviceDecommissionPlan.AffectedAuthority affected = plan.getAffectedAuthorities().get(0);
		Assert.assertEquals(Integer.valueOf(0), affected.getRemainingDeviceCount());
		Assert.assertTrue(affected.isProtectedAuthority());
		Assert.assertFalse(affected.isWillCascadeDelete());
	}

	private DeviceDecommissionServiceImpl newService(SmtDeviceAuthorityRelationService relationService,
													   SmtDeviceAuthorityService authorityService,
													   SmtStaffDeviceAuthService staffAuthService,
													   SmtVehicleApplyService vehicleApplyService,
													   SmtBusinessDeviceAuthService businessDeviceAuthService) {
		return new DeviceDecommissionServiceImpl(relationService, authorityService, staffAuthService,
				vehicleApplyService, businessDeviceAuthService, Mockito.mock(SmtDeviceService.class));
	}

	private SmtDeviceAuthorityRelation relation(Integer authId, String deviceId) {
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setAuthorityId(authId);
		relation.setDeviceId(deviceId);
		return relation;
	}

	private SmtStaffDeviceAuth staffAuth(Integer id, Long staffId, Integer authId) {
		SmtStaffDeviceAuth staffAuth = new SmtStaffDeviceAuth();
		staffAuth.setId(id);
		staffAuth.setStaffId(staffId);
		staffAuth.setAuthId(authId);
		return staffAuth;
	}

	@Test
	public void executeRevokesDeviceAndCascadeDeletesEmptyAuthority() {
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtDeviceAuthorityService authorityService = Mockito.mock(SmtDeviceAuthorityService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtVehicleApplyService vehicleApplyService = Mockito.mock(SmtVehicleApplyService.class);
		DeviceDecommissionServiceImpl service = newService(relationService, authorityService,
				staffAuthService, vehicleApplyService, Mockito.mock(SmtBusinessDeviceAuthService.class));

		DeviceDecommissionPlan plan = new DeviceDecommissionPlan();
		plan.setDeviceId("device-A");
		DeviceDecommissionPlan.AffectedAuthority affected = new DeviceDecommissionPlan.AffectedAuthority();
		affected.setAuthorityId(100);
		affected.setRemainingDeviceCount(0);
		affected.setWillCascadeDelete(true);
		plan.setAffectedAuthorities(Collections.singletonList(affected));

		service.execute(plan);

		Mockito.verify(authorityService).revokeDeviceAccess(100, "device-A");
		Mockito.verify(relationService).remove(Mockito.any());
		Mockito.verify(staffAuthService).removeByAuthId(100);
		Mockito.verify(vehicleApplyService).removeByAuthId(100);
		Mockito.verify(authorityService).removeById(100);
	}

	@Test
	public void executeOnlyUnbindsDeviceWhenAuthorityStaysAlive() {
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtDeviceAuthorityService authorityService = Mockito.mock(SmtDeviceAuthorityService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtVehicleApplyService vehicleApplyService = Mockito.mock(SmtVehicleApplyService.class);
		DeviceDecommissionServiceImpl service = newService(relationService, authorityService,
				staffAuthService, vehicleApplyService, Mockito.mock(SmtBusinessDeviceAuthService.class));

		DeviceDecommissionPlan plan = new DeviceDecommissionPlan();
		plan.setDeviceId("device-A");
		DeviceDecommissionPlan.AffectedAuthority affected = new DeviceDecommissionPlan.AffectedAuthority();
		affected.setAuthorityId(100);
		affected.setRemainingDeviceCount(1);
		affected.setWillCascadeDelete(false);
		plan.setAffectedAuthorities(Collections.singletonList(affected));

		service.execute(plan);

		Mockito.verify(authorityService).revokeDeviceAccess(100, "device-A");
		Mockito.verify(relationService).remove(Mockito.any());
		Mockito.verify(staffAuthService, Mockito.never()).removeByAuthId(Mockito.anyInt());
		Mockito.verify(vehicleApplyService, Mockito.never()).removeByAuthId(Mockito.anyInt());
		Mockito.verify(authorityService, Mockito.never()).removeById(Mockito.anyInt());
	}

	@Test
	public void decommissionDeviceRunsPlanExecuteThenDeletesDevice() {
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		Mockito.when(relationService.getRelationByDeviceId("device-A")).thenReturn(Collections.emptyList());
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		Mockito.when(deviceService.deleteDevice("device-A")).thenReturn(true);

		DeviceDecommissionServiceImpl service = new DeviceDecommissionServiceImpl(relationService,
				Mockito.mock(SmtDeviceAuthorityService.class),
				Mockito.mock(SmtStaffDeviceAuthService.class),
				Mockito.mock(SmtVehicleApplyService.class),
				Mockito.mock(SmtBusinessDeviceAuthService.class),
				deviceService);

		com.tce.smart.common.core.model.Result result = service.decommissionDevice("device-A");

		Assert.assertTrue(result.isSuccess());
		Mockito.verify(relationService).getRelationByDeviceId("device-A");
		Mockito.verify(deviceService).deleteDevice("device-A");
	}

	@Test
	public void decommissionDeviceRejectsBlankDeviceId() {
		DeviceDecommissionServiceImpl service = newService(
				Mockito.mock(SmtDeviceAuthorityRelationService.class),
				Mockito.mock(SmtDeviceAuthorityService.class),
				Mockito.mock(SmtStaffDeviceAuthService.class),
				Mockito.mock(SmtVehicleApplyService.class),
				Mockito.mock(SmtBusinessDeviceAuthService.class));

		com.tce.smart.common.core.model.Result result = service.decommissionDevice("  ");

		Assert.assertFalse(result.isSuccess());
	}
}
