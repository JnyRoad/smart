package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.feign.consume.RemoteRsEmpService;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.dto.securityzone.SecurityAuthDeleteTaskRef;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

	/**
	 * 自动删权必须返回每个实际落库任务的来源、主键、设备和动作，供审计关联表完整记录。
	 */
	@Test
	public void savePersonCardTasksWithResult_returnsAllTaskRefsWithActualSource() throws Exception {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		SmtStaff staff = staff();

		SmtDeviceAuthorityRelation normalRelation = deviceRelation("normal-device");
		SmtDeviceAuthorityRelation iscRelation = deviceRelation("isc-device");
		SmtDevice normalDevice = device("normal-device", DeviceSyncEnum.NO.getCode());
		SmtDevice iscDevice = device("isc-device", DeviceSyncEnum.YES.getCode());
		Mockito.when(deviceService.getById("normal-device")).thenReturn(normalDevice);
		Mockito.when(deviceService.getById("isc-device")).thenReturn(iscDevice);
		Mockito.when(deviceTaskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("101", "202");
		setField(service, "smtDeviceService", deviceService);
		setField(service, "smtDeviceTaskService", deviceTaskService);

		List<SecurityAuthDeleteTaskRef> refs = service.savePersonCardTasksWithResult(
				DeviceTaskActionEnum.DEL.getCode(), 1L, 2L, staff, Arrays.asList(normalRelation, iscRelation));
		Assert.assertEquals(2, refs.size());
		SecurityAuthDeleteTaskRef normalRef = refs.get(0);
		Assert.assertEquals("NORMAL", normalRef.getTaskSource());
		Assert.assertEquals("101", normalRef.getTaskId());
		Assert.assertEquals("normal-device", normalRef.getDeviceCode());
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), normalRef.getAction());

		SecurityAuthDeleteTaskRef iscRef = refs.get(1);
		Assert.assertEquals("ISC", iscRef.getTaskSource());
		Assert.assertEquals("202", iscRef.getTaskId());
		Assert.assertEquals("isc-device", iscRef.getDeviceCode());
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), iscRef.getAction());
	}

	/** 严格入口收到 saveTask 的错误文本时必须抛错，避免产生无效审计关联或半批删除。 */
	@Test
	public void savePersonCardTasksWithResult_rejectsNonNumericTaskResult() throws Exception {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		SmtStaff staff = staff();
		SmtDeviceAuthorityRelation relation = deviceRelation("normal-device");
		SmtDevice normalDevice = device("normal-device", DeviceSyncEnum.NO.getCode());
		Mockito.when(deviceService.getById("normal-device")).thenReturn(normalDevice);
		Mockito.when(deviceTaskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("任务已存在");
		setField(service, "smtDeviceService", deviceService);
		setField(service, "smtDeviceTaskService", deviceTaskService);

		try {
			service.savePersonCardTasksWithResult(DeviceTaskActionEnum.DEL.getCode(), 1L, 2L,
					staff, Collections.singletonList(relation));
			Assert.fail("saveTask 返回错误文本时严格入口必须抛错");
		} catch (RuntimeException ex) {
			Assert.assertEquals("设备任务创建失败", ex.getMessage());
		}
	}

	/** 后续设备未创建任务时整体抛错，调用方不得取得不完整的任务关联。 */
	@Test
	public void savePersonCardTasksWithResult_laterNullResult_rejectsPartialBatch() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffServiceImpl service = staffService(taskService, Mockito.mock(SmtIscDownRecordService.class));
		Mockito.when(taskService.saveTask(Mockito.any())).thenReturn("101", null);
		try {
			service.savePersonCardTasksWithResult(DeviceTaskActionEnum.DEL.getCode(), 1L, 2L,
					staff(), Arrays.asList(deviceRelation("device-1"), deviceRelation("device-2")));
			Assert.fail("后续设备返回空任务必须使整个调用失败");
		} catch (RuntimeException ex) {
			Assert.assertEquals("设备任务创建失败", ex.getMessage());
		}
		Mockito.verify(taskService, Mockito.times(2)).saveTask(Mockito.any());
	}

	/** 旧入口仍忽略空值与错误文本，并继续处理后续设备，避免影响原有调用方。 */
	@Test
	public void savePersonCardTask_legacyEntry_continuesAfterNonIdResults() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtStaffServiceImpl service = staffService(taskService, Mockito.mock(SmtIscDownRecordService.class));
		Mockito.when(taskService.saveTask(Mockito.any())).thenReturn(null, "任务已存在");

		service.savePersonCardTask(DeviceTaskActionEnum.DEL.getCode(), 1L, 2L,
				staff(), Arrays.asList(deviceRelation("device-1"), deviceRelation("device-2")));

		Mockito.verify(taskService, Mockito.times(2)).saveTask(Mockito.any());
	}

	/** 构造一条设备权限关系，设备编码与设备主键保持一致以避免测试混淆。 */
	private SmtDeviceAuthorityRelation deviceRelation(String deviceCode) {
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setDeviceId(deviceCode);
		relation.setAuthorityId(77);
		return relation;
	}

	/** 构造指定同步来源的设备。 */
	private SmtDevice device(String deviceCode, Integer isSync) {
		SmtDevice device = new SmtDevice();
		device.setId(deviceCode);
		device.setDeviceCode(deviceCode);
		device.setIsSync(isSync);
		return device;
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
