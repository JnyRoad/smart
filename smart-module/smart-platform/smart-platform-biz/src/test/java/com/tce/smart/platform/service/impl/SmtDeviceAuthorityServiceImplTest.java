package com.tce.smart.platform.service.impl;

import com.tce.smart.platform.api.dto.req.AreaTypeSwitchReqDTO;
import com.tce.smart.platform.api.dto.req.DeviceAuthRelationDelReqDTO;
import com.tce.smart.platform.api.dto.resp.AreaTypeConflictDeviceVO;
import com.tce.smart.platform.api.dto.resp.AreaTypeSwitchRespDTO;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.entity.SmtVehicleApply;
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

		// 三名员工同属本次待清空的权限(authId=100)：
		// - 第一位在另一个权限(authId=200)下仍保留 device-A 的访问权限，应从可删除设备里剔除
		// - 第二位、第三位没有其他权限，应整套 deviceIds 都可删除
		SmtStaffDeviceAuth firstStaffTargetAuth = staffAuth(1, 1001L, 100);
		SmtStaffDeviceAuth secondStaffTargetAuth = staffAuth(2, 1002L, 100);
		SmtStaffDeviceAuth thirdStaffTargetAuth = staffAuth(3, 1003L, 100);
		SmtStaffDeviceAuth firstStaffOtherAuth = staffAuth(4, 1001L, 200);
		Mockito.when(staffAuthService.list(Mockito.any()))
				.thenReturn(Arrays.asList(firstStaffTargetAuth, secondStaffTargetAuth, thirdStaffTargetAuth))
				.thenReturn(Collections.singletonList(firstStaffOtherAuth));
		Mockito.when(staffAuthService.removeAuthToDevice(Mockito.any(SmtStaffDeviceAuth.class), Mockito.anyList()))
				.thenReturn(true);

		service.deviceAuthRelationClear(100);

		// 核心回归点：无论本次清空涉及多少名员工，"查其他权限"和"查其他权限关联设备"都应只批量各查一次，
		// 而不是像修复前那样随员工数线性增长（N+1）。
		Mockito.verify(staffAuthService, Mockito.times(2)).list(Mockito.any());
		Mockito.verify(relationService, Mockito.times(2)).getRelationByAuthId(Mockito.anyList());

		ArgumentCaptor<SmtStaffDeviceAuth> staffAuthCaptor = ArgumentCaptor.forClass(SmtStaffDeviceAuth.class);
		ArgumentCaptor<List> deviceIdsCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(staffAuthService, Mockito.times(3))
				.removeAuthToDevice(staffAuthCaptor.capture(), deviceIdsCaptor.capture());

		Assert.assertEquals(firstStaffTargetAuth, staffAuthCaptor.getAllValues().get(0));
		Assert.assertTrue(deviceIdsCaptor.getAllValues().get(0).isEmpty());
		Assert.assertEquals(secondStaffTargetAuth, staffAuthCaptor.getAllValues().get(1));
		Assert.assertEquals(Collections.singletonList("device-A"), deviceIdsCaptor.getAllValues().get(1));
		Assert.assertEquals(thirdStaffTargetAuth, staffAuthCaptor.getAllValues().get(2));
		Assert.assertEquals(Collections.singletonList("device-A"), deviceIdsCaptor.getAllValues().get(2));
	}

	@Test
	public void deviceAuthRelationDelBatchesMetadataQueriesRegardlessOfSelectedCount() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService, staffAuthService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthorityRelation targetDevice = relation(100, "device-A");
		Mockito.when(relationService.getRelationByAuthId(Collections.singletonList(100)))
				.thenReturn(Collections.singletonList(targetDevice));

		// 三条待删除的人员权限记录，且都没有其他权限重叠 -> 批量查询应各自只调用一次
		SmtStaffDeviceAuth firstAuth = staffAuth(1, 1001L, 100);
		SmtStaffDeviceAuth secondAuth = staffAuth(2, 1002L, 100);
		SmtStaffDeviceAuth thirdAuth = staffAuth(3, 1003L, 100);
		Mockito.when(staffAuthService.listByIds(Arrays.asList(1, 2, 3)))
				.thenReturn(Arrays.asList(firstAuth, secondAuth, thirdAuth));
		Mockito.when(staffAuthService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(staffAuthService.removeAuthToDevice(Mockito.any(SmtStaffDeviceAuth.class), Mockito.anyList()))
				.thenReturn(true);

		DeviceAuthRelationDelReqDTO reqDTO = new DeviceAuthRelationDelReqDTO();
		reqDTO.setAuthId(100);
		reqDTO.setType(DeviceAuthTypeEnum.PERSON.getCode());
		reqDTO.setDelIds(Arrays.asList(1, 2, 3));

		service.deviceAuthRelationDel(reqDTO);

		// 核心回归点：批量删除 3 条记录，getById 全部合并为一次 listByIds，"其他权限"查询合并为一次批量 list，
		// 而不是修复前那样每条记录各查一次（N+1）。
		Mockito.verify(staffAuthService, Mockito.times(1)).listByIds(Arrays.asList(1, 2, 3));
		Mockito.verify(staffAuthService, Mockito.times(1)).list(Mockito.any());
		Mockito.verify(staffAuthService, Mockito.times(3))
				.removeAuthToDevice(Mockito.any(SmtStaffDeviceAuth.class), Mockito.anyList());
	}

	@Test
	public void deviceAuthRelationDelBatchesVehicleMetadataQueriesRegardlessOfSelectedCount() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtVehicleApplyService vehicleApplyService = Mockito.mock(SmtVehicleApplyService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService,
				Mockito.mock(SmtStaffDeviceAuthService.class), vehicleApplyService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthorityRelation targetDevice = relation(100, "device-A");
		Mockito.when(relationService.getRelationByAuthId(Collections.singletonList(100)))
				.thenReturn(Collections.singletonList(targetDevice));

		// 三条待删除的车辆权限记录，且都没有其他权限重叠 -> 批量查询应各自只调用一次
		SmtVehicleApply firstApply = vehicleApply(1, 2001L, 100);
		SmtVehicleApply secondApply = vehicleApply(2, 2002L, 100);
		SmtVehicleApply thirdApply = vehicleApply(3, 2003L, 100);
		Mockito.when(vehicleApplyService.listByIds(Arrays.asList(1, 2, 3)))
				.thenReturn(Arrays.asList(firstApply, secondApply, thirdApply));
		Mockito.when(vehicleApplyService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(vehicleApplyService.removeAuthToDevice(Mockito.any(SmtVehicleApply.class), Mockito.anyList()))
				.thenReturn(true);

		DeviceAuthRelationDelReqDTO reqDTO = new DeviceAuthRelationDelReqDTO();
		reqDTO.setAuthId(100);
		reqDTO.setType(DeviceAuthTypeEnum.VEHICLE.getCode());
		reqDTO.setDelIds(Arrays.asList(1, 2, 3));

		service.deviceAuthRelationDel(reqDTO);

		// 核心回归点：车辆分支与人员分支走同一套批量化逻辑，同样应该是固定次数的查询，不随记录数增长
		Mockito.verify(vehicleApplyService, Mockito.times(1)).listByIds(Arrays.asList(1, 2, 3));
		Mockito.verify(vehicleApplyService, Mockito.times(1)).list(Mockito.any());
		Mockito.verify(vehicleApplyService, Mockito.times(3))
				.removeAuthToDevice(Mockito.any(SmtVehicleApply.class), Mockito.anyList());
	}

	@Test
	public void switchAreaTypeUpdatesWhenNoConflictExists() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService, staffAuthService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setParkId(1);
		authority.setAreaType(1);
		Mockito.when(authorityMapper.selectById(100)).thenReturn(authority);
		Mockito.when(authorityMapper.findAreaTypeConflicts(100, 0)).thenReturn(Collections.emptyList());
		Mockito.when(authorityMapper.updateById(Mockito.any(SmtDeviceAuthority.class))).thenReturn(1);

		AreaTypeSwitchReqDTO reqDTO = new AreaTypeSwitchReqDTO();
		reqDTO.setId(100);
		reqDTO.setAreaType(0);

		Result<AreaTypeSwitchRespDTO> result = service.switchAreaType(reqDTO, Collections.singletonList(1));

		Assert.assertTrue(result.getData().isSuccess());
		Assert.assertTrue(result.getData().getConflicts().isEmpty());
		ArgumentCaptor<SmtDeviceAuthority> updateCaptor = ArgumentCaptor.forClass(SmtDeviceAuthority.class);
		Mockito.verify(authorityMapper).updateById(updateCaptor.capture());
		Assert.assertEquals(Integer.valueOf(100), updateCaptor.getValue().getId());
		Assert.assertEquals(Integer.valueOf(0), updateCaptor.getValue().getAreaType());
		// 核心回归点：不写库无关的下游数据，切换只应该动 area_type 这一个字段
		// 项目里 Mockito 版本较旧（Spring Boot 2.1 默认管理的 2.x），没有 verifyNoInteractions，
		// 用 verifyZeroInteractions 代替
		Mockito.verifyZeroInteractions(relationService, staffAuthService);
	}

	@Test
	public void switchAreaTypeBlocksAndListsConflictsWithoutWriting() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService, staffAuthService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setParkId(1);
		authority.setAreaType(1);
		Mockito.when(authorityMapper.selectById(100)).thenReturn(authority);

		AreaTypeConflictDeviceVO conflict = new AreaTypeConflictDeviceVO();
		conflict.setDeviceId("device-A");
		conflict.setDeviceName("1F-2区-超黑面检机-03");
		conflict.setConflictAuthorityId(200);
		conflict.setConflictAuthorityName("门禁_AB栋连廊");
		Mockito.when(authorityMapper.findAreaTypeConflicts(100, 0)).thenReturn(Collections.singletonList(conflict));

		AreaTypeSwitchReqDTO reqDTO = new AreaTypeSwitchReqDTO();
		reqDTO.setId(100);
		reqDTO.setAreaType(0);

		Result<AreaTypeSwitchRespDTO> result = service.switchAreaType(reqDTO, Collections.singletonList(1));

		Assert.assertFalse(result.getData().isSuccess());
		Assert.assertEquals(1, result.getData().getConflicts().size());
		Assert.assertEquals("device-A", result.getData().getConflicts().get(0).getDeviceId());
		// 核心回归点：存在冲突时绝不写库
		Mockito.verify(authorityMapper, Mockito.never()).updateById(Mockito.any());
	}

	@Test
	public void switchAreaTypeIsIdempotentWhenTargetEqualsCurrent() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService, staffAuthService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setParkId(1);
		authority.setAreaType(1);
		Mockito.when(authorityMapper.selectById(100)).thenReturn(authority);

		AreaTypeSwitchReqDTO reqDTO = new AreaTypeSwitchReqDTO();
		reqDTO.setId(100);
		reqDTO.setAreaType(1);

		Result<AreaTypeSwitchRespDTO> result = service.switchAreaType(reqDTO, Collections.singletonList(1));

		Assert.assertTrue(result.getData().isSuccess());
		// 核心回归点：目标性质和当前一致时幂等短路，既不查冲突也不写库
		Mockito.verify(authorityMapper, Mockito.never()).findAreaTypeConflicts(Mockito.anyInt(), Mockito.anyInt());
		Mockito.verify(authorityMapper, Mockito.never()).updateById(Mockito.any());
	}

	@Test
	public void switchAreaTypeSucceedsWhenAuthorityHasNoDevices() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService, staffAuthService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setParkId(1);
		authority.setAreaType(1);
		Mockito.when(authorityMapper.selectById(100)).thenReturn(authority);
		Mockito.when(authorityMapper.findAreaTypeConflicts(100, 0)).thenReturn(Collections.emptyList());
		Mockito.when(authorityMapper.updateById(Mockito.any(SmtDeviceAuthority.class))).thenReturn(1);

		AreaTypeSwitchReqDTO reqDTO = new AreaTypeSwitchReqDTO();
		reqDTO.setId(100);
		reqDTO.setAreaType(0);

		Result<AreaTypeSwitchRespDTO> result = service.switchAreaType(reqDTO, Collections.singletonList(1));

		Assert.assertTrue(result.getData().isSuccess());
		Mockito.verify(authorityMapper).updateById(Mockito.any());
	}

	@Test
	public void switchAreaTypeFailsWhenAuthorityOutsideCallerParkScope() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService, staffAuthService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setParkId(999);
		authority.setAreaType(1);
		Mockito.when(authorityMapper.selectById(100)).thenReturn(authority);

		AreaTypeSwitchReqDTO reqDTO = new AreaTypeSwitchReqDTO();
		reqDTO.setId(100);
		reqDTO.setAreaType(0);

		Result<AreaTypeSwitchRespDTO> result = service.switchAreaType(reqDTO, Collections.singletonList(1));

		Assert.assertFalse(result.getData().isSuccess());
		Mockito.verify(authorityMapper, Mockito.never()).updateById(Mockito.any());
	}

	@Test
	public void deviceAuthRelationClearKeepsDeviceScopeIndependentPerVehicle() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtVehicleApplyService vehicleApplyService = Mockito.mock(SmtVehicleApplyService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService,
				Mockito.mock(SmtStaffDeviceAuthService.class), vehicleApplyService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setType(DeviceAuthTypeEnum.VEHICLE.getCode());
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

		// 第一辆车在另一个权限(authorityId=200)下仍保留 device-A 的访问权限，应从可删除设备里剔除；第二辆车没有其他权限
		SmtVehicleApply firstVehicleTargetApply = vehicleApply(1, 2001L, 100);
		SmtVehicleApply secondVehicleTargetApply = vehicleApply(2, 2002L, 100);
		SmtVehicleApply firstVehicleOtherApply = vehicleApply(3, 2001L, 200);
		Mockito.when(vehicleApplyService.list(Mockito.any()))
				.thenReturn(Arrays.asList(firstVehicleTargetApply, secondVehicleTargetApply))
				.thenReturn(Collections.singletonList(firstVehicleOtherApply));
		Mockito.when(vehicleApplyService.removeAuthToDevice(Mockito.any(SmtVehicleApply.class), Mockito.anyList()))
				.thenReturn(true);

		service.deviceAuthRelationClear(100);

		Mockito.verify(vehicleApplyService, Mockito.times(2)).list(Mockito.any());

		ArgumentCaptor<SmtVehicleApply> vehicleApplyCaptor = ArgumentCaptor.forClass(SmtVehicleApply.class);
		ArgumentCaptor<List> deviceIdsCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(vehicleApplyService, Mockito.times(2))
				.removeAuthToDevice(vehicleApplyCaptor.capture(), deviceIdsCaptor.capture());

		Assert.assertEquals(firstVehicleTargetApply, vehicleApplyCaptor.getAllValues().get(0));
		Assert.assertTrue(deviceIdsCaptor.getAllValues().get(0).isEmpty());
		Assert.assertEquals(secondVehicleTargetApply, vehicleApplyCaptor.getAllValues().get(1));
		Assert.assertEquals(Collections.singletonList("device-A"), deviceIdsCaptor.getAllValues().get(1));
	}

	private SmtDeviceAuthorityServiceImpl newService(SmtDeviceAuthorityMapper authorityMapper,
													 SmtDeviceAuthorityRelationService relationService,
													 SmtStaffDeviceAuthService staffAuthService) {
		return newService(authorityMapper, relationService, staffAuthService, Mockito.mock(SmtVehicleApplyService.class));
	}

	private SmtDeviceAuthorityServiceImpl newService(SmtDeviceAuthorityMapper authorityMapper,
													 SmtDeviceAuthorityRelationService relationService,
													 SmtStaffDeviceAuthService staffAuthService,
													 SmtVehicleApplyService vehicleApplyService) {
		return new SmtDeviceAuthorityServiceImpl(
				Mockito.mock(SmtDeviceService.class),
				authorityMapper,
				relationService,
				Mockito.mock(SmtBusinessDeviceAuthService.class),
				staffAuthService,
				Mockito.mock(SmtDeviceMapper.class),
				Mockito.mock(SmtDeviceTaskService.class),
				Mockito.mock(SmtIscDeviceTaskService.class),
				vehicleApplyService,
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

	private SmtVehicleApply vehicleApply(Integer id, Long vehicleId, Integer authorityId) {
		SmtVehicleApply vehicleApply = new SmtVehicleApply();
		vehicleApply.setId(id);
		vehicleApply.setVehicleId(vehicleId);
		vehicleApply.setAuthorityId(authorityId);
		return vehicleApply;
	}

	private SmtDeviceAuthorityRelation relation(Integer authId, String deviceId) {
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setAuthorityId(authId);
		relation.setDeviceId(deviceId);
		return relation;
	}

	@Test
	public void revokeDeviceAccessOnlyRemovesGivenDeviceForPersonAuthority() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtBatchDeviceTaskService batchDeviceTaskService = Mockito.mock(SmtBatchDeviceTaskService.class);
		SmtDeviceAuthorityServiceImpl service = new SmtDeviceAuthorityServiceImpl(
				Mockito.mock(SmtDeviceService.class),
				authorityMapper,
				relationService,
				Mockito.mock(SmtBusinessDeviceAuthService.class),
				staffAuthService,
				Mockito.mock(SmtDeviceMapper.class),
				Mockito.mock(SmtDeviceTaskService.class),
				Mockito.mock(SmtIscDeviceTaskService.class),
				Mockito.mock(SmtVehicleApplyService.class),
				staffService,
				Mockito.mock(SmtVehicleMapper.class),
				Mockito.mock(SmtIscDeviceTaskServiceImpl.class),
				batchDeviceTaskService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setType(DeviceAuthTypeEnum.PERSON.getCode());
		Mockito.when(authorityMapper.selectById(100)).thenReturn(authority);

		SmtStaffDeviceAuth staffAuth = staffAuth(1, 1001L, 100);
		Mockito.when(staffAuthService.list(Mockito.any())).thenReturn(Collections.singletonList(staffAuth));

		SmtStaff staff = new SmtStaff();
		staff.setId(1001L);
		staff.setBadge("B001");
		staff.setName("张三");
		staff.setFacePicId("pic-1");
		Mockito.when(staffService.list(Mockito.any())).thenReturn(Collections.singletonList(staff));

		service.revokeDeviceAccess(100, "device-A");

		ArgumentCaptor<List> delListCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<List> addListCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(batchDeviceTaskService).createStaffFaceAuthTasks(
				Mockito.anyList(), delListCaptor.capture(), addListCaptor.capture());
		Assert.assertEquals(Collections.singletonList("device-A"), delListCaptor.getValue());
		Assert.assertTrue(addListCaptor.getValue().isEmpty());
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
