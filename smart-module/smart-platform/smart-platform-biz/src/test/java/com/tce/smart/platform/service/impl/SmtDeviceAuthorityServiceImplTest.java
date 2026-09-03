package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.api.dto.req.AreaTypeSwitchReqDTO;
import com.tce.smart.platform.api.dto.req.DeviceAuthRelationAddReqDTO;
import com.tce.smart.platform.api.dto.req.DeviceAuthRelationDelReqDTO;
import com.tce.smart.platform.api.dto.resp.AreaTypeConflictDeviceVO;
import com.tce.smart.platform.api.dto.resp.AreaTypeSwitchRespDTO;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
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
import com.tce.smart.platform.core.enums.StaffSyncEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SmtDeviceAuthorityServiceImplTest {

	/**
	 * Oracle IN 列表单批参数上限（超过即 ORA-01795），
	 * 加上批量查询里 ne/eq 等额外条件各占 1 个绑定参数，
	 * 单次查询的绑定参数总数不应超过 上限+1。
	 */
	private static final int MAX_BOUND_PARAMS_PER_QUERY = 1000 + 1;

	/**
	 * 纯单测环境没有 MyBatis-Plus 运行时，lambda 列名缓存不会自动初始化；
	 * 这里手动注册涉及实体的 TableInfo，测试才能渲染查询条件并统计 IN 绑定参数个数。
	 */
	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
		TableInfoHelper.initTableInfo(assistant, SmtStaffDeviceAuth.class);
		TableInfoHelper.initTableInfo(assistant, SmtVehicleApply.class);
		TableInfoHelper.initTableInfo(assistant, SmtStaff.class);
	}

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

	/**
	 * 员工侧「其他权限」批量查询：清空 1500 人的大权限组时，
	 * staffIds 的 IN 查询必须按 Oracle 上限(1000)分成两批，
	 * 且跨批员工的"其他权限保留设备"合并语义与单条 IN 完全一致。
	 * （修复前是单条 1500 参数的 IN，真实 Oracle 直接 ORA-01795 整个事务回滚）
	 */
	@Test
	public void deviceAuthRelationClearBatchesStaffOtherAuthQueriesOverOracleLimit() throws Exception {
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

		// 1500 名员工同属本次待清空的权限组(authId=100)，超过 Oracle IN 上限
		List<SmtStaffDeviceAuth> targetAuthList = IntStream.rangeClosed(1, 1500)
				.mapToObj(i -> staffAuth(i, (long) i, 100))
				.collect(Collectors.toList());
		// 第 1 批(前 1000 人)里的员工 1、第 2 批(后 500 人)里的员工 1401 各自还有另一个权限(200)覆盖 device-A
		Mockito.when(staffAuthService.list(Mockito.any()))
				.thenReturn(targetAuthList)
				.thenReturn(Collections.singletonList(staffAuth(9001, 1L, 200)))
				.thenReturn(Collections.singletonList(staffAuth(9002, 1401L, 200)));
		Mockito.when(staffAuthService.removeAuthToDevice(Mockito.any(SmtStaffDeviceAuth.class), Mockito.anyList()))
				.thenReturn(true);

		service.deviceAuthRelationClear(100);

		// 核心回归点 1：目标权限组 1 次 + 「其他权限」分批 2 次 = 共 3 次，且每次绑定参数都在 Oracle 上限内
		ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
		Mockito.verify(staffAuthService, Mockito.times(3)).list(wrapperCaptor.capture());
		assertEveryQueryWithinOracleInLimit(wrapperCaptor.getAllValues());

		// 核心回归点 2：合并语义与单条 IN 一致——跨批员工的其他权限设备照样被保留（从可删除列表剔除）
		ArgumentCaptor<List> deviceIdsCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(staffAuthService, Mockito.times(1500))
				.removeAuthToDevice(Mockito.any(SmtStaffDeviceAuth.class), deviceIdsCaptor.capture());
		List<List> removableLists = deviceIdsCaptor.getAllValues();
		// 员工 1（第 1 批）与员工 1401（第 2 批）在权限 200 下仍保留 device-A，可删除列表应为空
		Assert.assertTrue("第 1 批员工的其他权限设备应被保留", removableLists.get(0).isEmpty());
		Assert.assertTrue("第 2 批员工的其他权限设备应被保留", removableLists.get(1400).isEmpty());
		// 没有其他权限的员工，device-A 应整体可删除
		Assert.assertEquals(Collections.singletonList("device-A"), removableLists.get(1));
	}

	/**
	 * 车辆侧「其他权限」批量查询：与员工侧同构，
	 * 1500 辆车的 vehicleIds IN 查询必须分两批，跨批合并语义不变。
	 */
	@Test
	public void deviceAuthRelationClearBatchesVehicleOtherAuthQueriesOverOracleLimit() throws Exception {
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

		// 1500 辆车同属本次待清空的权限组，超过 Oracle IN 上限
		List<SmtVehicleApply> targetApplyList = IntStream.rangeClosed(1, 1500)
				.mapToObj(i -> vehicleApply(i, (long) i, 100))
				.collect(Collectors.toList());
		// 第 1 批里的车辆 1、第 2 批里的车辆 1401 各自还有另一个权限(200)覆盖 device-A
		Mockito.when(vehicleApplyService.list(Mockito.any()))
				.thenReturn(targetApplyList)
				.thenReturn(Collections.singletonList(vehicleApply(9001, 1L, 200)))
				.thenReturn(Collections.singletonList(vehicleApply(9002, 1401L, 200)));
		Mockito.when(vehicleApplyService.removeAuthToDevice(Mockito.any(SmtVehicleApply.class), Mockito.anyList()))
				.thenReturn(true);

		service.deviceAuthRelationClear(100);

		ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
		Mockito.verify(vehicleApplyService, Mockito.times(3)).list(wrapperCaptor.capture());
		assertEveryQueryWithinOracleInLimit(wrapperCaptor.getAllValues());

		ArgumentCaptor<List> deviceIdsCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(vehicleApplyService, Mockito.times(1500))
				.removeAuthToDevice(Mockito.any(SmtVehicleApply.class), deviceIdsCaptor.capture());
		List<List> removableLists = deviceIdsCaptor.getAllValues();
		Assert.assertTrue("第 1 批车辆的其他权限设备应被保留", removableLists.get(0).isEmpty());
		Assert.assertTrue("第 2 批车辆的其他权限设备应被保留", removableLists.get(1400).isEmpty());
		Assert.assertEquals(Collections.singletonList("device-A"), removableLists.get(1));
	}

	/**
	 * 批量授权：badges 来自 HTTP 外部输入，2500 个工号的 IN 查询必须分三批，
	 * 每批绑定参数都在 Oracle 上限内，且所有批次查到的员工都被授权。
	 * （修复前是单条 2500 参数的 IN，真实 Oracle 直接 ORA-01795 整批 500 回滚）
	 */
	@Test
	public void deviceAuthRelationAddBatchesBadgeQueriesOverOracleLimit() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
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
				Mockito.mock(SmtBatchDeviceTaskService.class));
		setField(service, "baseMapper", authorityMapper);

		// 该权限组暂无已授权员工、无关联设备（不生成设备任务，聚焦工号查询的分批行为）
		Mockito.when(staffAuthService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(relationService.list(Mockito.any())).thenReturn(Collections.emptyList());

		// 三批依次返回各自命中的员工（都在职、有人脸图片）
		Mockito.when(staffService.list(Mockito.any()))
				.thenReturn(buildStaffs(1, 1000))
				.thenReturn(buildStaffs(1001, 2000))
				.thenReturn(buildStaffs(2001, 2500));

		DeviceAuthRelationAddReqDTO reqDTO = new DeviceAuthRelationAddReqDTO();
		reqDTO.setAuthId(100);
		reqDTO.setType(DeviceAuthTypeEnum.PERSON.getCode());
		reqDTO.setBadges(IntStream.rangeClosed(1, 2500).mapToObj(i -> "B" + i).collect(Collectors.toList()));

		List<String> noExist = service.deviceAuthRelationAdd(reqDTO);

		// 核心回归点 1：2500 个工号拆成三批查询，每批绑定参数都在 Oracle 上限内
		ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
		Mockito.verify(staffService, Mockito.times(3)).list(wrapperCaptor.capture());
		assertEveryQueryWithinOracleInLimit(wrapperCaptor.getAllValues());

		// 核心回归点 2：三批命中的员工全部授权成功，无一遗漏
		ArgumentCaptor<Collection> savedAuthCaptor = ArgumentCaptor.forClass(Collection.class);
		Mockito.verify(staffAuthService, Mockito.times(1)).saveBatch(savedAuthCaptor.capture());
		Assert.assertEquals(2500, savedAuthCaptor.getValue().size());
		Assert.assertTrue(noExist.isEmpty());
	}

	/**
	 * 批量授权的去重语义：单条 IN 对重复参数天然只返回一份结果，
	 * 分批后同一工号若跨批出现会被重复授权——所以分批前必须先去重。
	 * 1001 个工号里首尾重复(B1 出现两次)，去重后恰好 1000 个 = 只查一批、只授权一次。
	 */
	@Test
	public void deviceAuthRelationAddDedupsBadgesBeforeBatching() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
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
				Mockito.mock(SmtBatchDeviceTaskService.class));
		setField(service, "baseMapper", authorityMapper);

		Mockito.when(staffAuthService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(relationService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(staffService.list(Mockito.any())).thenReturn(buildStaffs(1, 1000));

		// 1001 个工号：B1..B1000 + 重复的 B1（若不去重会拆成 1000+1 两批，B1 被授权两次）
		List<String> badges = IntStream.rangeClosed(1, 1000).mapToObj(i -> "B" + i)
				.collect(Collectors.toList());
		badges.add("B1");

		DeviceAuthRelationAddReqDTO reqDTO = new DeviceAuthRelationAddReqDTO();
		reqDTO.setAuthId(100);
		reqDTO.setType(DeviceAuthTypeEnum.PERSON.getCode());
		reqDTO.setBadges(badges);

		service.deviceAuthRelationAdd(reqDTO);

		// 去重后恰好 1000 个工号 = 单批查询，且绑定参数在 Oracle 上限内
		ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
		Mockito.verify(staffService, Mockito.times(1)).list(wrapperCaptor.capture());
		assertEveryQueryWithinOracleInLimit(wrapperCaptor.getAllValues());

		// 每个员工只授权一次，不因工号重复而重复授权
		ArgumentCaptor<Collection> savedAuthCaptor = ArgumentCaptor.forClass(Collection.class);
		Mockito.verify(staffAuthService, Mockito.times(1)).saveBatch(savedAuthCaptor.capture());
		Assert.assertEquals(1000, savedAuthCaptor.getValue().size());
		long distinctStaffCount = ((Collection<SmtStaffDeviceAuth>) savedAuthCaptor.getValue()).stream()
				.map(SmtStaffDeviceAuth::getStaffId).distinct().count();
		Assert.assertEquals(1000, distinctStaffCount);
	}

	/**
	 * 权限组批量下发的自定义窗口应同时写入非 ISC 任务与人员关联记录。
	 */
	@Test
	public void deviceAuthRelationAddSavesCustomValidityWindow() {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtDeviceAuthorityServiceImpl service = new SmtDeviceAuthorityServiceImpl(
				Mockito.mock(SmtDeviceService.class), authorityMapper, relationService,
				Mockito.mock(SmtBusinessDeviceAuthService.class), staffAuthService, deviceMapper,
				deviceTaskService, Mockito.mock(SmtIscDeviceTaskService.class),
				Mockito.mock(SmtVehicleApplyService.class), staffService, Mockito.mock(SmtVehicleMapper.class),
				Mockito.mock(SmtIscDeviceTaskServiceImpl.class), Mockito.mock(SmtBatchDeviceTaskService.class));

		SmtStaff staff = new SmtStaff();
		staff.setId(1001L);
		staff.setBadge("B1001");
		staff.setName("张三");
		staff.setFacePicId("face-1");
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setIsSync(StaffSyncEnum.NO.getCode());
		Mockito.when(staffAuthService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(relationService.list(Mockito.any())).thenReturn(Collections.singletonList(relation(100, "device-1")));
		Mockito.when(staffService.list(Mockito.any())).thenReturn(Collections.singletonList(staff));
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);

		DeviceAuthRelationAddReqDTO reqDTO = new DeviceAuthRelationAddReqDTO();
		reqDTO.setAuthId(100);
		reqDTO.setType(DeviceAuthTypeEnum.PERSON.getCode());
		reqDTO.setBadges(Collections.singletonList("B1001"));
		reqDTO.setStartTime("2026-09-03");
		reqDTO.setEndTime("2026-09-05");

		service.deviceAuthRelationAdd(reqDTO);

		ArgumentCaptor<Collection<SmtDeviceTask>> taskCaptor = ArgumentCaptor.forClass(Collection.class);
		Mockito.verify(deviceTaskService).saveBatch(taskCaptor.capture());
		SmtDeviceTask task = taskCaptor.getValue().iterator().next();
		Assert.assertEquals(Long.valueOf(LocalDate.of(2026, 9, 3).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()), task.getStartTime());
		Assert.assertEquals(Long.valueOf(LocalDate.of(2026, 9, 6).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() - 1), task.getOverTime());
		ArgumentCaptor<Collection<SmtStaffDeviceAuth>> relationCaptor = ArgumentCaptor.forClass(Collection.class);
		Mockito.verify(staffAuthService).saveBatch(relationCaptor.capture());
		SmtStaffDeviceAuth staffAuth = relationCaptor.getValue().iterator().next();
		Assert.assertEquals(LocalDate.of(2026, 9, 3), staffAuth.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		Assert.assertEquals(LocalDate.of(2026, 9, 5), staffAuth.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
	}

	/**
	 * 权限组日期倒置时必须在员工、设备和关联查询前失败，确保事务内零写入。
	 */
	@Test
	public void deviceAuthRelationAddRejectsInvertedValidityWindowBeforeWrites() {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService, staffAuthService);
		DeviceAuthRelationAddReqDTO reqDTO = new DeviceAuthRelationAddReqDTO();
		reqDTO.setAuthId(100);
		reqDTO.setType(DeviceAuthTypeEnum.PERSON.getCode());
		reqDTO.setBadges(Collections.singletonList("B1001"));
		reqDTO.setStartTime("2026-09-05");
		reqDTO.setEndTime("2026-09-03");

		try {
			service.deviceAuthRelationAdd(reqDTO);
			Assert.fail("倒置有效期不应进入批量授权流程");
		} catch (RuntimeException expected) {
			Mockito.verifyZeroInteractions(staffAuthService, relationService);
		}
	}

	/** 构造 [from, to] 区间的在职、有人脸图片的员工（id=i，工号 B+i） */
	private List<SmtStaff> buildStaffs(int from, int to) {
		List<SmtStaff> staffs = new ArrayList<>();
		for (int i = from; i <= to; i++) {
			SmtStaff staff = new SmtStaff();
			staff.setId((long) i);
			staff.setBadge("B" + i);
			staff.setName("员工" + i);
			staff.setFacePicId("pic-" + i);
			staffs.add(staff);
		}
		return staffs;
	}

	/**
	 * 断言每次查询实际绑定的参数个数都不超过 Oracle IN 上限（+1 个 ne/eq 附加条件）。
	 * MyBatis-Plus 的 in() 参数是渲染 SQL 时才绑定的，这里先强制渲染一次再统计。
	 */
	private void assertEveryQueryWithinOracleInLimit(List<Wrapper> wrappers) {
		for (Wrapper wrapper : wrappers) {
			AbstractWrapper<?, ?, ?> abstractWrapper = (AbstractWrapper<?, ?, ?>) wrapper;
			abstractWrapper.getSqlSegment();
			int boundParamCount = abstractWrapper.getParamNameValuePairs().size();
			Assert.assertTrue(
					"单次查询绑定参数应不超过 " + MAX_BOUND_PARAMS_PER_QUERY + " 个（IN≤1000 + 1 个附加条件），实际 " + boundParamCount + " 个",
					boundParamCount <= MAX_BOUND_PARAMS_PER_QUERY);
		}
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
