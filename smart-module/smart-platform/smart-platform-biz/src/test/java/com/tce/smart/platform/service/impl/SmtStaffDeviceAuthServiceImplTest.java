package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.core.dto.UpdateDeviceAuthDTO;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.mapper.SmtStaffDeviceAuthMapper;
import com.tce.smart.platform.core.service.SmtDeviceTaskDetailService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import com.tce.smart.platform.core.util.PermissionValidityWindow;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SmtStaffDeviceAuthServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDownRecord.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDeviceTask.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtStaffDeviceAuth.class);
	}

	@Test
	public void updateAuthNewCancelsIscTasksBeforeReissue() {
		SmtIscDownRecordService iscDownRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskService iscDeviceTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtTaskDownRecordService taskDownRecordService = Mockito.mock(SmtTaskDownRecordService.class);
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtStaffDeviceAuthServiceImpl service = Mockito.spy(new SmtStaffDeviceAuthServiceImpl(
				Mockito.mock(SmtStaffDeviceAuthMapper.class),
				deviceTaskService,
				iscDeviceTaskService,
				Mockito.mock(SmtParkBuService.class),
				Mockito.mock(SmtDeviceAuthorityRelationService.class),
				staffService,
				Mockito.mock(SmtDeviceTaskDetailService.class),
				taskDownRecordService,
				iscDownRecordService,
				Mockito.mock(RemoteDispatcherService.class)));

		SmtStaffDeviceAuth oldAuth = new SmtStaffDeviceAuth();
		oldAuth.setStaffId(1001L);
		oldAuth.setAuthId(2001);
		Mockito.doReturn(Collections.singletonList(oldAuth)).when(service).list(Mockito.any());
		Mockito.doReturn(true).when(service).saveBatch(Mockito.anyCollection());

		SmtStaff staff = new SmtStaff();
		staff.setId(1001L);
		staff.setBadge("1001");
		staff.setName("张三");
		staff.setStatus(StaffStatusEnum.STAFF_STATUS_IN.getCode());
		staff.setFacePicId("face-1");
		Mockito.when(staffService.getById(1001L)).thenReturn(staff);

		UpdateDeviceAuthDTO auth = new UpdateDeviceAuthDTO();
		auth.setIds(Collections.singletonList("1001"));
		auth.setDeviceAuthIds(Collections.emptyList());

		service.updateAuthNew(3, auth);

		Mockito.verify(taskDownRecordService).remove(Mockito.any());
		Mockito.verify(deviceTaskService).remove(Mockito.any());
		ArgumentCaptor<LambdaQueryWrapper> downRecordQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(iscDownRecordService).remove(downRecordQueryCaptor.capture());
		Mockito.verify(iscDeviceTaskService).cancelSupersededStaffAuthTasks("1001");
		assertStaffIscCleanupQuery(downRecordQueryCaptor.getValue());
		Mockito.verify(iscDownRecordService, Mockito.never()).list(Mockito.any());
		Mockito.verify(deviceTaskService).updateStaffAuthNew(
				Mockito.eq(staff),
				Mockito.eq(Collections.emptyList()),
				Mockito.eq(Collections.singletonList(2001)),
				Mockito.anyInt(),
				Mockito.anyString(),
				Mockito.eq(3),
				Mockito.anyMap());
	}

	/**
	 * 重新下发必须复用现有关联的自定义日期，而非退回请求默认日期。
	 */
	@Test
	public void updateAuthNewReissueUsesExistingCustomValidityWindow() {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtStaffDeviceAuthServiceImpl service = Mockito.spy(new SmtStaffDeviceAuthServiceImpl(
				Mockito.mock(SmtStaffDeviceAuthMapper.class), deviceTaskService,
				Mockito.mock(SmtIscDeviceTaskService.class), Mockito.mock(SmtParkBuService.class), relationService,
				staffService, Mockito.mock(SmtDeviceTaskDetailService.class),
				Mockito.mock(SmtTaskDownRecordService.class), Mockito.mock(SmtIscDownRecordService.class),
				Mockito.mock(RemoteDispatcherService.class)));
		SmtStaffDeviceAuth oldAuth = buildAuth(1, 1001L, 2001);
		oldAuth.setStartTime(Date.from(LocalDate.of(2026, 9, 3).atStartOfDay(ZoneId.systemDefault()).toInstant()));
		oldAuth.setEndTime(Date.from(LocalDate.of(2026, 9, 5).atStartOfDay(ZoneId.systemDefault()).toInstant()));
		Mockito.doReturn(Collections.singletonList(oldAuth)).when(service).list(Mockito.any());
		Mockito.doReturn(true).when(service).saveBatch(Mockito.anyCollection());
		SmtDeviceAuthorityRelation deviceRelation = new SmtDeviceAuthorityRelation();
		deviceRelation.setAuthorityId(2001);
		deviceRelation.setDeviceId("device-1");
		Mockito.when(relationService.list(Mockito.any())).thenReturn(Collections.singletonList(deviceRelation));
		SmtStaff staff = buildStaff(1001L, StaffStatusEnum.STAFF_STATUS_IN.getCode());
		Mockito.when(staffService.getById(1001L)).thenReturn(staff);

		UpdateDeviceAuthDTO auth = new UpdateDeviceAuthDTO();
		auth.setIds(Collections.singletonList("1001"));
		auth.setDeviceAuthIds(Collections.emptyList());
		service.updateAuthNew(3, auth);

		ArgumentCaptor<Map> validityWindowCaptor = ArgumentCaptor.forClass(Map.class);
		Mockito.verify(deviceTaskService).updateStaffAuthNew(
				Mockito.eq(staff), Mockito.eq(Collections.emptyList()), Mockito.eq(Collections.singletonList(2001)),
				Mockito.eq(DeviceTaskConstants.CARD_STAFF_IMPORT), Mockito.anyString(), Mockito.eq(3),
				validityWindowCaptor.capture());
		PermissionValidityWindow window = (PermissionValidityWindow) validityWindowCaptor.getValue().get("device-1");
		Assert.assertEquals(LocalDate.of(2026, 9, 3).atStartOfDay(ZoneId.systemDefault()).toEpochSecond(), window.getStartTime());
		Assert.assertEquals(LocalDate.of(2026, 9, 6).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() - 1, window.getOverTime());
	}

	/**
	 * 员工窗口提交的日期必须同时写入关联记录和设备任务，任务不以延迟删除方式失效。
	 */
	@Test
	public void updateAuthNewSavesAndPropagatesCustomValidityWindow() {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtStaffDeviceAuthServiceImpl service = newSpyService(deviceTaskService, staffService);
		Mockito.doReturn(Collections.emptyList()).when(service).list(Mockito.any());
		Mockito.doReturn(true).when(service).saveBatch(Mockito.anyCollection());

		SmtStaff staff = buildStaff(1001L, StaffStatusEnum.STAFF_STATUS_IN.getCode());
		Mockito.when(staffService.getById(1001L)).thenReturn(staff);

		UpdateDeviceAuthDTO auth = new UpdateDeviceAuthDTO();
		auth.setIds(Collections.singletonList("1001"));
		auth.setDeviceAuthIds(Collections.singletonList(2001));
		auth.setStartTime("2026-09-03");
		auth.setEndTime("2026-09-05");

		service.updateAuthNew(1, auth);

		ArgumentCaptor<Collection<SmtStaffDeviceAuth>> relationCaptor = ArgumentCaptor.forClass(Collection.class);
		Mockito.verify(service).saveBatch(relationCaptor.capture());
		SmtStaffDeviceAuth relation = relationCaptor.getValue().iterator().next();
		Assert.assertEquals(LocalDate.of(2026, 9, 3), relation.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		Assert.assertEquals(LocalDate.of(2026, 9, 5), relation.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		Mockito.verify(deviceTaskService).updateStaffAuthNew(
				Mockito.eq(staff), Mockito.eq(Collections.emptyList()), Mockito.eq(Collections.singletonList(2001)),
				Mockito.eq(DeviceTaskConstants.CARD_STAFF_IMPORT), Mockito.anyString(), Mockito.eq(1),
				Mockito.anyMap());
	}

	/**
	 * 倒置有效期必须在员工关联和设备任务写入前失败。
	 */
	@Test
	public void updateAuthNewRejectsInvertedValidityWindowBeforeWrites() {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffDeviceAuthServiceImpl service = newSpyService(deviceTaskService, Mockito.mock(SmtStaffService.class));
		UpdateDeviceAuthDTO auth = new UpdateDeviceAuthDTO();
		auth.setIds(Collections.singletonList("1001"));
		auth.setDeviceAuthIds(Collections.singletonList(2001));
		auth.setStartTime("2026-09-05");
		auth.setEndTime("2026-09-03");

		try {
			service.updateAuthNew(1, auth);
			Assert.fail("倒置有效期不应进入授权写入流程");
		} catch (RuntimeException expected) {
			Mockito.verify(service, Mockito.never()).list(Mockito.any());
			Mockito.verify(service, Mockito.never()).saveBatch(Mockito.anyCollection());
			Mockito.verifyZeroInteractions(deviceTaskService);
		}
	}

	@Test
	public void applyAuthDiffOnlyTouchesChangedAuthsAndKeepsPersonalOnes() {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtStaffDeviceAuthServiceImpl service = newSpyService(deviceTaskService, staffService);

		SmtStaffDeviceAuth unitAuth = buildAuth(11, 1001L, 2001);
		SmtStaffDeviceAuth personalAuth = buildAuth(12, 1001L, 2002);
		Mockito.doReturn(Arrays.asList(unitAuth, personalAuth)).when(service).list(Mockito.<LambdaQueryWrapper<SmtStaffDeviceAuth>>any());
		Mockito.doReturn(true).when(service).saveBatch(Mockito.anyCollection());
		Mockito.doReturn(true).when(service).removeByIds(Mockito.anyCollection());

		SmtStaff staff = buildStaff(1001L, StaffStatusEnum.STAFF_STATUS_IN.getCode());
		Mockito.when(staffService.getById(1001L)).thenReturn(staff);

		service.applyAuthDiff(Collections.singletonList(1001L),
				Collections.singletonList(2003), Collections.singletonList(2001));

		Mockito.verify(service).removeByIds(Mockito.eq(Collections.singletonList(11)));
		ArgumentCaptor<Collection<SmtStaffDeviceAuth>> saveCaptor = ArgumentCaptor.forClass(Collection.class);
		Mockito.verify(service).saveBatch(saveCaptor.capture());
		List<Integer> savedAuthIds = saveCaptor.getValue().stream().map(SmtStaffDeviceAuth::getAuthId).collect(Collectors.toList());
		Assert.assertEquals(Collections.singletonList(2003), savedAuthIds);
		Mockito.verify(deviceTaskService).updateStaffAuth(
				Mockito.eq(staff),
				Mockito.eq(Arrays.asList(2001, 2002)),
				Mockito.eq(Arrays.asList(2002, 2003)),
				Mockito.eq(DeviceTaskConstants.CARD_STAFF_IMPORT));
	}

	@Test
	public void applyAuthDiffSkipsStaffWithoutChanges() {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtStaffDeviceAuthServiceImpl service = newSpyService(deviceTaskService, staffService);

		SmtStaffDeviceAuth personalAuth = buildAuth(12, 1001L, 2002);
		Mockito.doReturn(Collections.singletonList(personalAuth)).when(service).list(Mockito.<LambdaQueryWrapper<SmtStaffDeviceAuth>>any());

		// 被删的权限本来就没有，新增的权限已存在 => 不应有任何写操作和下发任务
		service.applyAuthDiff(Collections.singletonList(1001L),
				Collections.singletonList(2002), Collections.singletonList(2001));

		Mockito.verify(service, Mockito.never()).removeByIds(Mockito.anyCollection());
		Mockito.verify(service, Mockito.never()).saveBatch(Mockito.anyCollection());
		Mockito.verify(deviceTaskService, Mockito.never()).updateStaffAuth(
				Mockito.any(SmtStaff.class), Mockito.anyList(), Mockito.anyList(), Mockito.anyInt());
	}

	@Test
	public void applyAuthDiffIssuesDeleteTaskWhenAllAuthsRemoved() {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtStaffDeviceAuthServiceImpl service = newSpyService(deviceTaskService, staffService);

		SmtStaffDeviceAuth unitAuth = buildAuth(11, 1001L, 2001);
		Mockito.doReturn(Collections.singletonList(unitAuth)).when(service).list(Mockito.<LambdaQueryWrapper<SmtStaffDeviceAuth>>any());
		Mockito.doReturn(true).when(service).removeByIds(Mockito.anyCollection());

		SmtStaff staff = buildStaff(1001L, StaffStatusEnum.STAFF_STATUS_IN.getCode());
		Mockito.when(staffService.getById(1001L)).thenReturn(staff);

		// 单位删掉员工仅有的权限：删除关联并以空的新权限列表下发删除任务
		service.applyAuthDiff(Collections.singletonList(1001L),
				Collections.<Integer>emptyList(), Collections.singletonList(2001));

		Mockito.verify(service).removeByIds(Mockito.eq(Collections.singletonList(11)));
		Mockito.verify(service, Mockito.never()).saveBatch(Mockito.anyCollection());
		Mockito.verify(deviceTaskService).updateStaffAuth(
				Mockito.eq(staff),
				Mockito.eq(Collections.singletonList(2001)),
				Mockito.eq(Collections.<Integer>emptyList()),
				Mockito.eq(DeviceTaskConstants.CARD_STAFF_IMPORT));
	}

	@Test
	public void applyAuthDiffSkipsTaskForQuitStaff() {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtStaffDeviceAuthServiceImpl service = newSpyService(deviceTaskService, staffService);

		SmtStaffDeviceAuth unitAuth = buildAuth(11, 1001L, 2001);
		Mockito.doReturn(Collections.singletonList(unitAuth)).when(service).list(Mockito.<LambdaQueryWrapper<SmtStaffDeviceAuth>>any());
		Mockito.doReturn(true).when(service).removeByIds(Mockito.anyCollection());

		SmtStaff staff = buildStaff(1001L, StaffStatusEnum.STAFF_STATUS_QUIT.getCode());
		Mockito.when(staffService.getById(1001L)).thenReturn(staff);

		service.applyAuthDiff(Collections.singletonList(1001L),
				Collections.<Integer>emptyList(), Collections.singletonList(2001));

		Mockito.verify(service).removeByIds(Mockito.eq(Collections.singletonList(11)));
		Mockito.verify(deviceTaskService, Mockito.never()).updateStaffAuth(
				Mockito.any(SmtStaff.class), Mockito.anyList(), Mockito.anyList(), Mockito.anyInt());
	}

 @Test
 public void acceptedEmployeeDeleteRetainsSourceEvenWithoutDeviceList() {
  SmtStaffDeviceAuthServiceImpl service=newSpyService(Mockito.mock(SmtDeviceTaskService.class),Mockito.mock(SmtStaffService.class));
  EmployeeAuthOperationAdapter adapter=Mockito.mock(EmployeeAuthOperationAdapter.class);
  org.springframework.test.util.ReflectionTestUtils.setField(service,"employeeAuthOperationAdapter",adapter);
  Mockito.when(adapter.removeRows(Collections.singletonList(11),null)).thenReturn(true);
  Mockito.doReturn(true).when(service).removeByIds(Mockito.anyCollection());
  Assert.assertTrue(service.removeAuthToDevice(Collections.singletonList(11),Collections.emptyList()));
  Mockito.verify(service,Mockito.never()).removeByIds(Mockito.anyCollection());
 }
 @Test
 public void rejectedBatchLeavesOldEmployeeRelationUntouched() {
  SmtStaffDeviceAuthServiceImpl service=newSpyService(Mockito.mock(SmtDeviceTaskService.class),Mockito.mock(SmtStaffService.class));
  EmployeeAuthOperationAdapter adapter=Mockito.mock(EmployeeAuthOperationAdapter.class);
  org.springframework.test.util.ReflectionTestUtils.setField(service,"employeeAuthOperationAdapter",adapter);
  Mockito.when(adapter.removeRows(Collections.singletonList(11),null)).thenThrow(new IllegalStateException("受理失败"));
  Mockito.doReturn(true).when(service).removeByIds(Mockito.anyCollection());
  try {service.removeAuthToDevice(Collections.singletonList(11),Collections.emptyList());Assert.fail("失败必须传播");}
  catch(IllegalStateException expected){Mockito.verify(service,Mockito.never()).removeByIds(Mockito.anyCollection());}
 }

	private SmtStaffDeviceAuthServiceImpl newSpyService(SmtDeviceTaskService deviceTaskService, SmtStaffService staffService) {
		return Mockito.spy(new SmtStaffDeviceAuthServiceImpl(
				Mockito.mock(SmtStaffDeviceAuthMapper.class),
				deviceTaskService,
				Mockito.mock(SmtIscDeviceTaskService.class),
				Mockito.mock(SmtParkBuService.class),
				Mockito.mock(SmtDeviceAuthorityRelationService.class),
				staffService,
				Mockito.mock(SmtDeviceTaskDetailService.class),
				Mockito.mock(SmtTaskDownRecordService.class),
				Mockito.mock(SmtIscDownRecordService.class),
				Mockito.mock(RemoteDispatcherService.class)));
	}

	private SmtStaffDeviceAuth buildAuth(Integer id, Long staffId, Integer authId) {
		SmtStaffDeviceAuth auth = new SmtStaffDeviceAuth();
		auth.setId(id);
		auth.setStaffId(staffId);
		auth.setAuthId(authId);
		return auth;
	}

	private SmtStaff buildStaff(Long id, Integer status) {
		SmtStaff staff = new SmtStaff();
		staff.setId(id);
		staff.setBadge("1001");
		staff.setName("张三");
		staff.setStatus(status);
		staff.setFacePicId("face-1");
		return staff;
	}

	private void assertStaffIscTaskCleanupQuery(LambdaQueryWrapper query) {
		String sqlSegment = query.getSqlSegment().toUpperCase();
		Assert.assertTrue(sqlSegment.contains("CARD_NO"));
		Assert.assertTrue(sqlSegment.contains("DEVICE_TYPE"));
		Assert.assertTrue(sqlSegment.contains("SERVICE_TYPE"));
		// 重新下发清理必须排除在途任务：STATUS != DOING(3) OR STATUS IS NULL
		Assert.assertTrue(sqlSegment.contains("STATUS"));
		Assert.assertTrue(sqlSegment.contains("IS NULL"));
		Collection<Object> params = query.getParamNameValuePairs().values();
		Assert.assertTrue(params.stream().anyMatch(value -> "1001".equals(String.valueOf(value))));
		Assert.assertTrue(params.stream().anyMatch(value -> DeviceTaskConstants.CARD.toString().equals(String.valueOf(value))));
		Assert.assertTrue(params.stream().anyMatch(value -> DeviceTaskConstants.CARD_STAFF_IMPORT.toString().equals(String.valueOf(value))));
		Assert.assertTrue(params.stream().anyMatch(value -> DeviceTaskConstants.UPDATE_FACE.toString().equals(String.valueOf(value))));
		Assert.assertTrue(params.stream().anyMatch(value ->
				String.valueOf(DeviceTaskStatusEnum.DOING.getCode()).equals(String.valueOf(value))));
		Assert.assertFalse(params.stream().anyMatch(value -> DeviceTaskConstants.CARD_ADMITTANCE.toString().equals(String.valueOf(value))));
	}

	private void assertStaffIscCleanupQuery(LambdaQueryWrapper query) {
		String sqlSegment = query.getSqlSegment().toUpperCase();
		Assert.assertTrue(sqlSegment.contains("CARD_NO"));
		Assert.assertTrue(sqlSegment.contains("DEVICE_TYPE"));
		Assert.assertTrue(sqlSegment.contains("SERVICE_TYPE"));
		Collection<Object> params = query.getParamNameValuePairs().values();
		Assert.assertTrue(params.stream().anyMatch(value -> "1001".equals(String.valueOf(value))));
		Assert.assertTrue(params.stream().anyMatch(value -> DeviceTaskConstants.CARD.toString().equals(String.valueOf(value))));
		Assert.assertTrue(params.stream().anyMatch(value -> DeviceTaskConstants.CARD_STAFF_IMPORT.toString().equals(String.valueOf(value))));
		Assert.assertTrue(params.stream().anyMatch(value -> DeviceTaskConstants.UPDATE_FACE.toString().equals(String.valueOf(value))));
		Assert.assertFalse(params.stream().anyMatch(value -> DeviceTaskConstants.CARD_VISITOR.toString().equals(String.valueOf(value))));
		Assert.assertFalse(params.stream().anyMatch(value -> DeviceTaskConstants.CARD_ADMITTANCE.toString().equals(String.valueOf(value))));
	}
}
