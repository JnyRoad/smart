package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.feign.consume.RemoteRsEmpService;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.dto.SecurityAuthDispatchContext;
import com.tce.smart.platform.core.entity.SmtBusinessDeviceAuth;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.entity.SmtTaskDownRecord;
import com.tce.smart.platform.core.enums.DeviceSyncEnum;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.platform.core.service.SmtIscStaffCardService;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import com.tce.smart.platform.service.SmtBusinessDeviceAuthService;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtSecurityBuService;
import com.tce.smart.platform.service.SmtStaffDeviceAuthService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.BusinessAuthorityEnum;
import com.tce.smart.tool.enums.DeviceAuthorityEnum;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Locale;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtStaffServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtStaffDeviceAuth.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDownRecord.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtTaskDownRecord.class);
	}

	@Test
	public void addDeviceTaskCreatesUpdateFaceTasksForStaffFaceUpdates() throws Exception {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtIscDownRecordService iscDownRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtStaffServiceImpl service = staffService(deviceTaskService, iscDownRecordService);
		SmtStaff staff = staff();
		Mockito.when(iscDownRecordService.getOne(Mockito.any())).thenReturn(new SmtIscDownRecord());

		service.addDeviceTask(staff, DeviceTaskActionEnum.UPDATE.getCode());

		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(deviceTaskService).saveTask(taskCaptor.capture());
		DeviceTaskVO task = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.UPDATE.getCode(), task.getAction());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.UPDATE_FACE, task.getServiceType());
		Assert.assertEquals("face-new", task.getImageId());
		Assert.assertEquals("device-2", task.getDeviceCode());
	}

	@Test
	public void addDeviceTaskKeepsUpdateWithoutExistingIscRecordAsInitialCardDownload() throws Exception {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtIscDownRecordService iscDownRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtStaffServiceImpl service = staffService(deviceTaskService, iscDownRecordService);
		SmtStaff staff = staff();
		Mockito.when(iscDownRecordService.getOne(Mockito.any())).thenReturn(null);

		service.addDeviceTask(staff, DeviceTaskActionEnum.UPDATE.getCode());

		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(deviceTaskService).saveTask(taskCaptor.capture());
		DeviceTaskVO task = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.DOWN.getCode(), task.getAction());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getServiceType());
	}

	@Test
	public void addDeviceTaskKeepsDelayUpdateWithoutExistingIscRecordAsInitialDelayedCardDownload() throws Exception {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtIscDownRecordService iscDownRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtStaffServiceImpl service = staffService(deviceTaskService, iscDownRecordService);
		SmtStaff staff = staff();
		Mockito.when(iscDownRecordService.getOne(Mockito.any())).thenReturn(null);

		service.addDeviceTask(staff, DeviceTaskActionEnum.DELAY_UPDATE.getCode());

		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(deviceTaskService).saveTask(taskCaptor.capture());
		DeviceTaskVO task = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.DELAY_DOWN.getCode(), task.getAction());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getServiceType());
	}

	@Test
	public void addDeviceTaskKeepsCardServiceTypeForNonIscStaffFaceUpdates() throws Exception {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtIscDownRecordService iscDownRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtTaskDownRecordService taskDownRecordService = Mockito.mock(SmtTaskDownRecordService.class);
		SmtStaffServiceImpl service = staffService(deviceTaskService, iscDownRecordService, taskDownRecordService,
				DeviceSyncEnum.NO.getCode());
		SmtStaff staff = staff();
		Mockito.when(taskDownRecordService.getOne(Mockito.any())).thenReturn(new SmtTaskDownRecord());

		service.addDeviceTask(staff, DeviceTaskActionEnum.UPDATE.getCode());

		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(deviceTaskService).saveTask(taskCaptor.capture());
		DeviceTaskVO task = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.UPDATE.getCode(), task.getAction());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getServiceType());
		ArgumentCaptor<LambdaQueryWrapper> downRecordQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(taskDownRecordService).getOne(downRecordQueryCaptor.capture());
		assertQuerySqlContains(downRecordQueryCaptor.getValue(), "DEVICE_TYPE");
		assertQuerySqlContains(downRecordQueryCaptor.getValue(), "SERVICE_TYPE");
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD);
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD_STAFF_IMPORT);
		Mockito.verify(iscDownRecordService, Mockito.never()).getOne(Mockito.any());
	}

	@Test
	public void addDeviceTaskKeepsCardServiceTypeForInitialStaffFaceDownloads() throws Exception {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffServiceImpl service = staffService(deviceTaskService, Mockito.mock(SmtIscDownRecordService.class));
		SmtStaff staff = staff();

		service.addDeviceTask(staff, DeviceTaskActionEnum.DOWN.getCode());

		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(deviceTaskService).saveTask(taskCaptor.capture());
		DeviceTaskVO task = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.DOWN.getCode(), task.getAction());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getServiceType());
	}

	@Test
	public void updatePersonCardKeepsInitialIscStaffDownloadsAsCardTasks() throws Exception {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffDeviceAuthService staffDeviceAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityRelationService deviceAuthorityRelationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService iscDownRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		SmtStaff staff = staff();
		SmtStaffDeviceAuth staffDeviceAuth = new SmtStaffDeviceAuth();
		staffDeviceAuth.setAuthId(77);
		SmtDeviceAuthorityRelation deviceAuthorityRelation = new SmtDeviceAuthorityRelation();
		deviceAuthorityRelation.setDeviceId("isc-device-1");
		SmtDevice iscDevice = new SmtDevice();
		iscDevice.setId("isc-device-1");
		iscDevice.setIsSync(DeviceSyncEnum.YES.getCode());
		Mockito.when(staffDeviceAuthService.list(Mockito.any())).thenReturn(Collections.singletonList(staffDeviceAuth));
		Mockito.when(deviceAuthorityRelationService.list(Mockito.any())).thenReturn(Collections.singletonList(deviceAuthorityRelation));
		Mockito.when(deviceService.getById("isc-device-1")).thenReturn(iscDevice);
		Mockito.when(iscDownRecordService.getOne(Mockito.any())).thenReturn(null);
		Mockito.when(deviceTaskService.saveTask(Mockito.any())).thenReturn("123");
		setField(service, "smtStaffDeviceAuthService", staffDeviceAuthService);
		setField(service, "smtDeviceAuthorityRelationService", deviceAuthorityRelationService);
		setField(service, "smtDeviceService", deviceService);
		setField(service, "smtIscDownRecordService", iscDownRecordService);
		setField(service, "smtDeviceTaskService", deviceTaskService);

		service.updatePersonCard(staff, "AQID", "face-new", null, null, null);

		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(deviceTaskService).saveTask(taskCaptor.capture());
		DeviceTaskVO task = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.DOWN.getCode(), task.getAction());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getServiceType());
		Assert.assertNull("通用入口不得隐式带入保密区来源", task.getSourceType());
	}

	@Test
	public void updatePersonCardForSecurityDispatchAddsExplicitSourceAndIntentMetadata() throws Exception {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffDeviceAuthService staffDeviceAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityRelationService deviceAuthorityRelationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService iscDownRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		SmtStaff staff = staff();
		SmtStaffDeviceAuth staffDeviceAuth = new SmtStaffDeviceAuth();
		staffDeviceAuth.setAuthId(77);
		SmtDeviceAuthorityRelation deviceAuthorityRelation = new SmtDeviceAuthorityRelation();
		deviceAuthorityRelation.setDeviceId("isc-device-1");
		SmtDevice iscDevice = new SmtDevice();
		iscDevice.setId("isc-device-1");
		iscDevice.setIsSync(DeviceSyncEnum.YES.getCode());
		Mockito.when(deviceAuthorityRelationService.list(Mockito.any()))
				.thenReturn(Collections.singletonList(deviceAuthorityRelation));
		Mockito.when(deviceService.getById("isc-device-1")).thenReturn(iscDevice);
		Mockito.when(iscDownRecordService.getOne(Mockito.any())).thenReturn(null);
		Mockito.when(deviceTaskService.saveTask(Mockito.any())).thenReturn("123");
		setField(service, "smtStaffDeviceAuthService", staffDeviceAuthService);
		setField(service, "smtDeviceAuthorityRelationService", deviceAuthorityRelationService);
		setField(service, "smtDeviceService", deviceService);
		setField(service, "smtIscDownRecordService", iscDownRecordService);
		setField(service, "smtDeviceTaskService", deviceTaskService);

		service.updatePersonCardForSecurityDispatch(staff, "AQID", "face-new",
				Collections.singletonList(staffDeviceAuth), null, "APPLY",
				SecurityAuthDispatchContext.of(1001L, 101L, 9002L, staff.getId(), 77));

		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(deviceTaskService).saveTask(taskCaptor.capture());
		DeviceTaskVO task = taskCaptor.getValue();
		Assert.assertEquals("SECURITY_AUTH", task.getSourceType());
		Assert.assertEquals(Long.valueOf(1001L), task.getSourceId());
		Assert.assertEquals(Long.valueOf(101L), task.getSourceDetailId());
		Assert.assertEquals(Long.valueOf(9002L), task.getBatchId());
		Assert.assertEquals("SECURITY_AUTH:" + staff.getId() + ":77:isc-device-1", task.getIntentKey());
		Assert.assertNull("保密区来源不得复用入厂 APPLY_ID", task.getApplyId());
	}

	@Test
	public void updatePersonCardCreatesUpdateFaceTasksForExistingIscStaffDownloads() throws Exception {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffDeviceAuthService staffDeviceAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityRelationService deviceAuthorityRelationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService iscDownRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		SmtStaff staff = staff();
		SmtStaffDeviceAuth staffDeviceAuth = new SmtStaffDeviceAuth();
		staffDeviceAuth.setAuthId(77);
		SmtDeviceAuthorityRelation deviceAuthorityRelation = new SmtDeviceAuthorityRelation();
		deviceAuthorityRelation.setDeviceId("isc-device-1");
		SmtDevice iscDevice = new SmtDevice();
		iscDevice.setId("isc-device-1");
		iscDevice.setIsSync(DeviceSyncEnum.YES.getCode());
		Mockito.when(staffDeviceAuthService.list(Mockito.any())).thenReturn(Collections.singletonList(staffDeviceAuth));
		Mockito.when(deviceAuthorityRelationService.list(Mockito.any())).thenReturn(Collections.singletonList(deviceAuthorityRelation));
		Mockito.when(deviceService.getById("isc-device-1")).thenReturn(iscDevice);
		Mockito.when(iscDownRecordService.getOne(Mockito.any())).thenReturn(new SmtIscDownRecord());
		Mockito.when(deviceTaskService.saveTask(Mockito.any())).thenReturn("123");
		setField(service, "smtStaffDeviceAuthService", staffDeviceAuthService);
		setField(service, "smtDeviceAuthorityRelationService", deviceAuthorityRelationService);
		setField(service, "smtDeviceService", deviceService);
		setField(service, "smtIscDownRecordService", iscDownRecordService);
		setField(service, "smtDeviceTaskService", deviceTaskService);

		service.updatePersonCard(staff, "AQID", "face-new", null, null, null);

		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(deviceTaskService).saveTask(taskCaptor.capture());
		DeviceTaskVO task = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.UPDATE.getCode(), task.getAction());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.UPDATE_FACE, task.getServiceType());
		ArgumentCaptor<LambdaQueryWrapper> downRecordQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(iscDownRecordService).getOne(downRecordQueryCaptor.capture());
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD);
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD_STAFF_IMPORT);
	}

	@Test
	public void updatePersonCardKeepsCardServiceTypeForExistingStandardDeviceDownloads() throws Exception {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffDeviceAuthService staffDeviceAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityRelationService deviceAuthorityRelationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtTaskDownRecordService taskDownRecordService = Mockito.mock(SmtTaskDownRecordService.class);
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		SmtStaff staff = staff();
		SmtStaffDeviceAuth staffDeviceAuth = new SmtStaffDeviceAuth();
		staffDeviceAuth.setAuthId(77);
		SmtDeviceAuthorityRelation deviceAuthorityRelation = new SmtDeviceAuthorityRelation();
		deviceAuthorityRelation.setDeviceId("standard-device-1");
		SmtDevice standardDevice = new SmtDevice();
		standardDevice.setId("standard-device-1");
		standardDevice.setIsSync(DeviceSyncEnum.NO.getCode());
		Mockito.when(staffDeviceAuthService.list(Mockito.any())).thenReturn(Collections.singletonList(staffDeviceAuth));
		Mockito.when(deviceAuthorityRelationService.list(Mockito.any())).thenReturn(Collections.singletonList(deviceAuthorityRelation));
		Mockito.when(deviceService.getById("standard-device-1")).thenReturn(standardDevice);
		Mockito.when(taskDownRecordService.getOne(Mockito.any())).thenReturn(new SmtTaskDownRecord());
		Mockito.when(deviceTaskService.saveTask(Mockito.any())).thenReturn("123");
		setField(service, "smtStaffDeviceAuthService", staffDeviceAuthService);
		setField(service, "smtDeviceAuthorityRelationService", deviceAuthorityRelationService);
		setField(service, "smtDeviceService", deviceService);
		setField(service, "smtTaskDownRecordService", taskDownRecordService);
		setField(service, "smtDeviceTaskService", deviceTaskService);

		service.updatePersonCard(staff, "AQID", "face-new", null, null, null);

		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(deviceTaskService).saveTask(taskCaptor.capture());
		DeviceTaskVO task = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.UPDATE.getCode(), task.getAction());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getServiceType());
		ArgumentCaptor<LambdaQueryWrapper> downRecordQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(taskDownRecordService).getOne(downRecordQueryCaptor.capture());
		assertQuerySqlContains(downRecordQueryCaptor.getValue(), "DEVICE_TYPE");
		assertQuerySqlContains(downRecordQueryCaptor.getValue(), "SERVICE_TYPE");
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD);
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD_STAFF_IMPORT);
	}

	@Test
	public void getEmpCardReturnsLocalIndependentCardBeforeHrFallback() throws Exception {
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		RemoteRsEmpService remoteRsEmpService = Mockito.mock(RemoteRsEmpService.class);
		Mockito.when(staffCardService.getFirstActiveCardNoByBadge("JA26086")).thenReturn("67890");
		setField(service, "smtIscStaffCardService", staffCardService);
		setField(service, "remoteRsEmpService", remoteRsEmpService);

		Assert.assertEquals("67890", service.getEmpCard("JA26086"));

		Mockito.verify(remoteRsEmpService, Mockito.never()).getEmpCard(Mockito.anyString(), Mockito.anyString());
	}

	@Test
	public void getEmpCardFallsBackToHrWhenNoLocalIndependentCardExists() throws Exception {
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		RemoteRsEmpService remoteRsEmpService = Mockito.mock(RemoteRsEmpService.class);
		Mockito.when(staffCardService.getFirstActiveCardNoByBadge("JA26086")).thenReturn(null);
		Mockito.when(remoteRsEmpService.getEmpCard("JA26086", SecurityConstants.FROM_IN))
				.thenReturn(Result.success("HR12345"));
		setField(service, "smtIscStaffCardService", staffCardService);
		setField(service, "remoteRsEmpService", remoteRsEmpService);

		Assert.assertEquals("HR12345", service.getEmpCard("JA26086"));
	}

	private SmtStaffServiceImpl staffService(SmtDeviceTaskService deviceTaskService,
											 SmtIscDownRecordService iscDownRecordService) throws Exception {
		return staffService(deviceTaskService, iscDownRecordService, Mockito.mock(SmtTaskDownRecordService.class),
				DeviceSyncEnum.YES.getCode());
	}

	private SmtStaffServiceImpl staffService(SmtDeviceTaskService deviceTaskService,
											 SmtIscDownRecordService iscDownRecordService,
											 SmtTaskDownRecordService taskDownRecordService,
											 Integer deviceSync) throws Exception {
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		SmtParkBuService parkBuService = Mockito.mock(SmtParkBuService.class);
		SmtSecurityBuService securityBuService = Mockito.mock(SmtSecurityBuService.class);
		SmtBusinessDeviceAuthService businessDeviceAuthService = Mockito.mock(SmtBusinessDeviceAuthService.class);
		SmtStaffDeviceAuthService staffDeviceAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityRelationService deviceAuthorityRelationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtPark park = new SmtPark();
		park.setId(5000022);
		SmtBusinessDeviceAuth businessDeviceAuth = new SmtBusinessDeviceAuth();
		businessDeviceAuth.setAuthId(77);
		SmtDeviceAuthorityRelation deviceAuthorityRelation = new SmtDeviceAuthorityRelation();
		deviceAuthorityRelation.setDeviceId("device-2");
		SmtDevice iscDevice = new SmtDevice();
		iscDevice.setId("device-2");
		iscDevice.setIsSync(deviceSync);
		Mockito.when(parkBuService.getParkListByBu(1001L)).thenReturn(Collections.singletonList(park));
		Mockito.when(securityBuService.getRelationSecuritys(Mockito.eq("1001"), Mockito.anyList()))
				.thenReturn(Collections.emptyList());
		Mockito.when(businessDeviceAuthService.getMulDeviceAuth(Mockito.anyList(),
				Mockito.eq(BusinessAuthorityEnum.STAFF_FACE.getCode())))
				.thenReturn(Collections.singletonList(businessDeviceAuth));
		Mockito.when(staffDeviceAuthService.count(Mockito.any())).thenReturn(1);
		Mockito.when(deviceAuthorityRelationService.getMulRelationAuth(Mockito.eq(1001L), Mockito.anyList(),
				Mockito.eq(BusinessAuthorityEnum.STAFF_FACE.getCode()), Mockito.eq(DeviceAuthorityEnum.STAFF)))
				.thenReturn(Collections.singletonList(deviceAuthorityRelation));
		Mockito.when(deviceService.getById("device-2")).thenReturn(iscDevice);
		setField(service, "smtParkBuService", parkBuService);
		setField(service, "smtSecurityBuService", securityBuService);
		setField(service, "smtBusinessDeviceAuthService", businessDeviceAuthService);
		setField(service, "smtStaffDeviceAuthService", staffDeviceAuthService);
		setField(service, "smtDeviceAuthorityRelationService", deviceAuthorityRelationService);
		setField(service, "smtDeviceTaskService", deviceTaskService);
		setField(service, "smtDeviceService", deviceService);
		setField(service, "smtIscDownRecordService", iscDownRecordService);
		setField(service, "smtTaskDownRecordService", taskDownRecordService);
		return service;
	}

	private SmtStaff staff() {
		SmtStaff staff = new SmtStaff();
		staff.setId(1001L);
		staff.setBadge("JA26086");
		staff.setName("张珂");
		staff.setCompId("1001");
		staff.setFacePicId("face-new");
		return staff;
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private void assertQueryHasParam(LambdaQueryWrapper queryWrapper, Object expected) {
		queryWrapper.getSqlSegment();
		Assert.assertTrue("query params: " + queryWrapper.getParamNameValuePairs(),
				queryWrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(expected).equals(String.valueOf(value))));
	}

	private void assertQuerySqlContains(LambdaQueryWrapper queryWrapper, String expected) {
		String sqlSegment = queryWrapper.getSqlSegment().toUpperCase(Locale.ROOT);
		Assert.assertTrue("query sql: " + sqlSegment, sqlSegment.contains(expected));
	}
}
