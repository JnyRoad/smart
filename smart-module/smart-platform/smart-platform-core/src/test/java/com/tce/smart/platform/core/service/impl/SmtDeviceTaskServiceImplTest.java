package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.dto.DeviceTaskDeleteDTO;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtTaskDownRecord;
import com.tce.smart.platform.core.enums.StaffSyncEnum;
import com.tce.smart.platform.core.mapper.SmtDeviceAuthorityRelationMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceTaskMapper;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtDeviceTaskServiceImplTest {

	/**
	 * 初始化 Lambda 查询所需的 MyBatis-Plus 元数据缓存。
	 */
	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtTaskDownRecord.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtDeviceTask.class);
	}

	/**
	 * 批量删除时每台符合条件的非 ISC 设备都应各自生成删除任务。
	 */
	@Test
	public void deleteTaskCreatesTasksForEveryNonIscDevice() throws Exception {
		SmtTaskDownRecordService downRecordService = Mockito.mock(SmtTaskDownRecordService.class);
		SmtDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtDeviceTaskServiceImpl service = new SmtDeviceTaskServiceImpl(downRecordService,
				Mockito.mock(SmtIscDownRecordService.class),
				Mockito.mock(SmtDeviceAuthorityRelationMapper.class),
				deviceMapper,
				Mockito.mock(SmtIscDeviceTaskService.class));
		setField(service, "baseMapper", deviceTaskMapper);
		SmtDevice device = new SmtDevice();
		device.setIsSync(StaffSyncEnum.NO.getCode());
		Mockito.when(deviceMapper.selectById(Mockito.anyString())).thenReturn(device);
		Mockito.when(downRecordService.getOne(Mockito.any()))
				.thenReturn(newDownRecord("device-1"), newDownRecord("device-2"), newDownRecord("device-3"));
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtDeviceTask.class))).thenReturn(1);
		DeviceTaskDeleteDTO request = new DeviceTaskDeleteDTO();
		request.setCardNo("1001");
		request.setDeviceCode(Arrays.asList("device-1", "device-2", "device-3"));

		Assert.assertTrue(service.deleteTask(request));

		ArgumentCaptor<SmtDeviceTask> taskCaptor = ArgumentCaptor.forClass(SmtDeviceTask.class);
		Mockito.verify(deviceTaskMapper, Mockito.times(3)).insert(taskCaptor.capture());
		Assert.assertEquals(Arrays.asList("device-1", "device-2", "device-3"), Arrays.asList(
				taskCaptor.getAllValues().get(0).getDeviceCode(),
				taskCaptor.getAllValues().get(1).getDeviceCode(),
				taskCaptor.getAllValues().get(2).getDeviceCode()));
	}

	/**
	 * 员工手动下发应将日期窗口交给统一任务入口；该入口会按设备类型路由至 ISC 或普通任务表。
	 */
	@Test
	public void updateStaffAuthNewForwardsManualValidityWindowToTaskRouting() {
		SmtDeviceAuthorityRelationMapper relationMapper = Mockito.mock(SmtDeviceAuthorityRelationMapper.class);
		SmtDeviceTaskServiceImpl service = Mockito.spy(new SmtDeviceTaskServiceImpl(
				Mockito.mock(SmtTaskDownRecordService.class), Mockito.mock(SmtIscDownRecordService.class), relationMapper,
				Mockito.mock(SmtDeviceMapper.class), Mockito.mock(SmtIscDeviceTaskService.class)));
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setAuthorityId(2001);
		relation.setDeviceId("isc-device-1");
		Mockito.when(relationMapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(relation));
		Mockito.doReturn("task-1").when(service).saveTask(Mockito.any(DeviceTaskVO.class));
		SmtStaff staff = new SmtStaff();
		staff.setId(1001L);
		staff.setBadge("B1001");
		staff.setName("张三");
		staff.setFacePicId("face-1");

		service.updateStaffAuthNew(staff, Collections.<Integer>emptyList(), Collections.singletonList(2001),
				DeviceTaskConstants.CARD_STAFF_IMPORT, null, 3, 1788393600L, 1788652799L);

		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(service).saveTask(taskCaptor.capture());
		Assert.assertEquals(Long.valueOf(1788393600L), taskCaptor.getValue().getStartTime());
		Assert.assertEquals(Long.valueOf(1788652799L), taskCaptor.getValue().getOverTime());
		Assert.assertEquals(DeviceTaskActionEnum.DOWN.getCode(), taskCaptor.getValue().getAction());
	}

	/**
	 * 覆盖权限时，同一设备仍需生成新的下发任务，以刷新设备上的权限有效期。
	 */
	@Test
	public void updateDeviceAuthNewReissuesSharedDeviceWhenOverwritingPermission() {
		SmtTaskDownRecordService downRecordService = Mockito.mock(SmtTaskDownRecordService.class);
		SmtDeviceAuthorityRelationMapper relationMapper = Mockito.mock(SmtDeviceAuthorityRelationMapper.class);
		SmtDeviceTaskServiceImpl service = Mockito.spy(new SmtDeviceTaskServiceImpl(
				downRecordService, Mockito.mock(SmtIscDownRecordService.class), relationMapper,
				Mockito.mock(SmtDeviceMapper.class), Mockito.mock(SmtIscDeviceTaskService.class)));
		SmtDeviceAuthorityRelation sharedRelation = new SmtDeviceAuthorityRelation();
		sharedRelation.setAuthorityId(2001);
		sharedRelation.setDeviceId("device-shared");
		SmtDeviceAuthorityRelation replacementSharedRelation = new SmtDeviceAuthorityRelation();
		replacementSharedRelation.setAuthorityId(2002);
		replacementSharedRelation.setDeviceId("device-shared");
		SmtDeviceAuthorityRelation replacementNewRelation = new SmtDeviceAuthorityRelation();
		replacementNewRelation.setAuthorityId(2002);
		replacementNewRelation.setDeviceId("device-new");
		SmtTaskDownRecord sharedDownRecord = newDownRecord("device-shared");
		Mockito.when(relationMapper.selectList(Mockito.any()))
				.thenReturn(Collections.singletonList(sharedRelation))
				.thenReturn(Arrays.asList(replacementSharedRelation, replacementNewRelation));
		Mockito.when(downRecordService.list(Mockito.any())).thenReturn(Collections.singletonList(sharedDownRecord));
		Mockito.doReturn("task-1").when(service).saveTask(Mockito.any(DeviceTaskVO.class));

		service.updateDeviceAuthNew(Collections.singletonList(2002), Collections.singletonList(2001), "1001",
				DeviceTaskConstants.CARD_STAFF_IMPORT, DeviceTaskConstants.CARD, false, "face-1", null,
				"B1001-张三", 2, 1788393600L, 1788652799L);

		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(service, Mockito.times(2)).saveTask(taskCaptor.capture());
		Assert.assertEquals(Arrays.asList("device-new", "device-shared"), Arrays.asList(
				taskCaptor.getAllValues().get(0).getDeviceCode(),
				taskCaptor.getAllValues().get(1).getDeviceCode()));
		Assert.assertEquals(Long.valueOf(1788393600L), taskCaptor.getAllValues().get(1).getStartTime());
		Assert.assertEquals(Long.valueOf(1788652799L), taskCaptor.getAllValues().get(1).getOverTime());
	}

	/**
	 * 单台任务保存失败不应阻断同批后续设备的删除任务创建。
	 */
	@Test
	public void deleteTaskContinuesAfterOneDeviceTaskSaveFails() throws Exception {
		SmtTaskDownRecordService downRecordService = Mockito.mock(SmtTaskDownRecordService.class);
		SmtDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtDeviceTaskServiceImpl service = new SmtDeviceTaskServiceImpl(downRecordService,
				Mockito.mock(SmtIscDownRecordService.class),
				Mockito.mock(SmtDeviceAuthorityRelationMapper.class),
				deviceMapper,
				Mockito.mock(SmtIscDeviceTaskService.class));
		setField(service, "baseMapper", deviceTaskMapper);
		SmtDevice device = new SmtDevice();
		device.setIsSync(StaffSyncEnum.NO.getCode());
		Mockito.when(deviceMapper.selectById(Mockito.anyString())).thenReturn(device);
		Mockito.when(downRecordService.getOne(Mockito.any()))
				.thenReturn(newDownRecord("device-1"), newDownRecord("device-2"), newDownRecord("device-3"));
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtDeviceTask.class))).thenReturn(0, 1, 1);
		DeviceTaskDeleteDTO request = new DeviceTaskDeleteDTO();
		request.setCardNo("1001");
		request.setDeviceCode(Arrays.asList("device-1", "device-2", "device-3"));

		Assert.assertFalse(service.deleteTask(request));

		Mockito.verify(deviceTaskMapper, Mockito.times(3)).insert(Mockito.any(SmtDeviceTask.class));
	}

	/**
	 * 未知设备不应阻断同批后续有效设备的删除任务创建。
	 */
	@Test
	public void deleteTaskContinuesAfterUnknownDeviceCode() throws Exception {
		SmtTaskDownRecordService downRecordService = Mockito.mock(SmtTaskDownRecordService.class);
		SmtDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtDeviceTaskServiceImpl service = new SmtDeviceTaskServiceImpl(downRecordService,
				Mockito.mock(SmtIscDownRecordService.class),
				Mockito.mock(SmtDeviceAuthorityRelationMapper.class),
				deviceMapper,
				Mockito.mock(SmtIscDeviceTaskService.class));
		setField(service, "baseMapper", deviceTaskMapper);
		SmtDevice device = new SmtDevice();
		device.setIsSync(StaffSyncEnum.NO.getCode());
		Mockito.when(deviceMapper.selectById(Mockito.anyString())).thenReturn(null, device);
		Mockito.when(downRecordService.getOne(Mockito.any())).thenReturn(newDownRecord("device-1"));
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtDeviceTask.class))).thenReturn(1);
		DeviceTaskDeleteDTO request = new DeviceTaskDeleteDTO();
		request.setCardNo("1001");
		request.setDeviceCode(Arrays.asList("missing-device", "device-1"));

		boolean result;
		try {
			result = service.deleteTask(request);
		} catch (NullPointerException exception) {
			Assert.fail("未知设备不应阻断后续设备的删除任务创建");
			return;
		}

		Assert.assertFalse(result);
		ArgumentCaptor<SmtDeviceTask> taskCaptor = ArgumentCaptor.forClass(SmtDeviceTask.class);
		Mockito.verify(deviceTaskMapper).insert(taskCaptor.capture());
		Assert.assertEquals("device-1", taskCaptor.getValue().getDeviceCode());
	}

	/**
	 * 访客删除应忽略历史完成任务，并为门禁卡下发记录新建删除任务。
	 */
	@Test
	public void delVisitorDeviceAuthHandlesAdmittanceCardRecordsAndIgnoresHistoricalDeletes() throws Exception {
		SmtTaskDownRecordService downRecordService = Mockito.mock(SmtTaskDownRecordService.class);
		SmtIscDeviceTaskService iscDeviceTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtDeviceTaskMapper.class);
		SmtDeviceTaskServiceImpl service = new SmtDeviceTaskServiceImpl(downRecordService,
				Mockito.mock(SmtIscDownRecordService.class),
				Mockito.mock(SmtDeviceAuthorityRelationMapper.class),
				Mockito.mock(SmtDeviceMapper.class),
				iscDeviceTaskService);
		setField(service, "baseMapper", deviceTaskMapper);
		Long fellowId = 3001L;
		SmtTaskDownRecord downRecord = new SmtTaskDownRecord();
		downRecord.setCardNo(fellowId.toString());
		downRecord.setDeviceCode("device-1");
		downRecord.setDeviceType(DeviceTaskConstants.CARD);
		downRecord.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
		SmtDeviceTask historicalDeleteTask = new SmtDeviceTask();
		historicalDeleteTask.setId(9001);
		historicalDeleteTask.setStatus(DeviceTaskStatusEnum.SUCCESS.getCode());
		historicalDeleteTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		Mockito.when(downRecordService.list(Mockito.any())).thenReturn(Collections.singletonList(downRecord));
		Mockito.when(deviceTaskMapper.selectList(Mockito.any()))
				.thenReturn(Collections.singletonList(historicalDeleteTask))
				.thenReturn(Collections.emptyList());
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtDeviceTask.class))).thenReturn(1);

		service.delVisitorDeviceAuth(fellowId);

		ArgumentCaptor<LambdaQueryWrapper> downRecordQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(downRecordService).list(downRecordQueryCaptor.capture());
		String downRecordSql = downRecordQueryCaptor.getValue().getSqlSegment().toUpperCase();
		Assert.assertTrue(downRecordSql.contains("DEVICE_TYPE"));
		Assert.assertTrue(downRecordQueryCaptor.getValue().getParamNameValuePairs().values().stream()
				.anyMatch(value -> DeviceTaskConstants.CARD_ADMITTANCE.toString().equals(String.valueOf(value))));
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD);
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CAR);
		ArgumentCaptor<LambdaQueryWrapper> taskQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(deviceTaskMapper, Mockito.times(2)).selectList(taskQueryCaptor.capture());
		assertQueryHasParam(taskQueryCaptor.getAllValues().get(0), DeviceTaskConstants.CARD);
		assertQueryHasParam(taskQueryCaptor.getAllValues().get(0), DeviceTaskConstants.CARD_ADMITTANCE);
		Mockito.verify(deviceTaskMapper, Mockito.never()).updateById(Mockito.eq(historicalDeleteTask));
		ArgumentCaptor<SmtDeviceTask> insertedDeleteTask = ArgumentCaptor.forClass(SmtDeviceTask.class);
		Mockito.verify(deviceTaskMapper).insert(insertedDeleteTask.capture());
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), insertedDeleteTask.getValue().getAction());
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), insertedDeleteTask.getValue().getStatus());
		Assert.assertEquals(DeviceTaskConstants.CARD_ADMITTANCE, insertedDeleteTask.getValue().getServiceType());
		Mockito.verify(iscDeviceTaskService).delVisitorDeviceAuth(fellowId);
	}

	/**
	 * 访客删除应复用处理中车辆门禁删除任务，而不重复插入。
	 */
	@Test
	public void delVisitorDeviceAuthReusesDoingCarAdmittanceDeleteTask() throws Exception {
		SmtTaskDownRecordService downRecordService = Mockito.mock(SmtTaskDownRecordService.class);
		SmtIscDeviceTaskService iscDeviceTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtDeviceTaskMapper.class);
		SmtDeviceTaskServiceImpl service = new SmtDeviceTaskServiceImpl(downRecordService,
				Mockito.mock(SmtIscDownRecordService.class),
				Mockito.mock(SmtDeviceAuthorityRelationMapper.class),
				Mockito.mock(SmtDeviceMapper.class),
				iscDeviceTaskService);
		setField(service, "baseMapper", deviceTaskMapper);
		Long vehicleId = 4001L;
		SmtTaskDownRecord downRecord = new SmtTaskDownRecord();
		downRecord.setCardNo(vehicleId.toString());
		downRecord.setDeviceCode("car-device-1");
		downRecord.setDeviceType(DeviceTaskConstants.CAR);
		downRecord.setServiceType(DeviceTaskConstants.CAT_ADMITTANCE);
		SmtDeviceTask doingDeleteTask = new SmtDeviceTask();
		doingDeleteTask.setId(9002);
		doingDeleteTask.setStatus(DeviceTaskStatusEnum.DOING.getCode());
		doingDeleteTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		doingDeleteTask.setCode(202);
		Mockito.when(downRecordService.list(Mockito.any())).thenReturn(Collections.singletonList(downRecord));
		Mockito.when(deviceTaskMapper.selectList(Mockito.any()))
				.thenReturn(Collections.singletonList(doingDeleteTask))
				.thenReturn(Collections.emptyList());

		service.delVisitorDeviceAuth(vehicleId);

		ArgumentCaptor<LambdaQueryWrapper> downRecordQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(downRecordService).list(downRecordQueryCaptor.capture());
		String downRecordSql = downRecordQueryCaptor.getValue().getSqlSegment().toUpperCase();
		Assert.assertTrue(downRecordSql.contains("DEVICE_TYPE"));
		Assert.assertTrue(downRecordQueryCaptor.getValue().getParamNameValuePairs().values().stream()
				.anyMatch(value -> DeviceTaskConstants.CAT_ADMITTANCE.toString().equals(String.valueOf(value))));
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD);
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CAR);
		ArgumentCaptor<LambdaQueryWrapper> taskQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(deviceTaskMapper, Mockito.times(2)).selectList(taskQueryCaptor.capture());
		assertQueryHasParam(taskQueryCaptor.getAllValues().get(0), DeviceTaskConstants.CAR);
		assertQueryHasParam(taskQueryCaptor.getAllValues().get(0), DeviceTaskConstants.CAT_ADMITTANCE);
		Mockito.verify(deviceTaskMapper).updateById(doingDeleteTask);
		Mockito.verify(deviceTaskMapper, Mockito.never()).insert(Mockito.any(SmtDeviceTask.class));
		Assert.assertEquals(DeviceTaskStatusEnum.DOING.getCode(), doingDeleteTask.getStatus());
		Assert.assertEquals(Integer.valueOf(202), doingDeleteTask.getCode());
		Mockito.verify(iscDeviceTaskService).delVisitorDeviceAuth(vehicleId);
	}

	/**
	 * 删除任务在调用方未填写类型时应从下发记录补齐类型。
	 */
	@Test
	public void saveTaskDeleteBackfillsDownRecordTypesWhenCallerOmitsThem() throws Exception {
		SmtTaskDownRecordService downRecordService = Mockito.mock(SmtTaskDownRecordService.class);
		SmtDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtDeviceTaskServiceImpl service = new SmtDeviceTaskServiceImpl(downRecordService,
				Mockito.mock(SmtIscDownRecordService.class),
				Mockito.mock(SmtDeviceAuthorityRelationMapper.class),
				deviceMapper,
				Mockito.mock(SmtIscDeviceTaskService.class));
		setField(service, "baseMapper", deviceTaskMapper);
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setDeviceName("device-1");
		device.setIsSync(StaffSyncEnum.NO.getCode());
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(0);
		SmtTaskDownRecord downRecord = new SmtTaskDownRecord();
		downRecord.setCardNo("1001");
		downRecord.setDeviceCode("device-1");
		downRecord.setDeviceType(DeviceTaskConstants.CARD);
		downRecord.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		Mockito.when(downRecordService.getOne(Mockito.any())).thenReturn(downRecord);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtDeviceTask.class))).thenAnswer(invocation -> {
			SmtDeviceTask task = invocation.getArgument(0);
			task.setId(8001);
			return 1;
		});
		DeviceTaskVO taskVO = new DeviceTaskVO();
		taskVO.setAction(DeviceTaskActionEnum.DEL.getCode());
		taskVO.setDeviceCode("device-1");
		taskVO.setCardNo("1001");
		taskVO.setStartTime(1780360200L);
		taskVO.setOverTime(1780394400L);

		String taskId = service.saveTask(taskVO);

		Assert.assertEquals("8001", taskId);
		ArgumentCaptor<SmtDeviceTask> insertedTask = ArgumentCaptor.forClass(SmtDeviceTask.class);
		Mockito.verify(deviceTaskMapper).insert(insertedTask.capture());
		Assert.assertEquals(DeviceTaskConstants.CARD, insertedTask.getValue().getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD_STAFF_IMPORT, insertedTask.getValue().getServiceType());
	}

	/**
	 * 历史员工人脸下发记录应规范为员工权限删除任务。
	 */
	@Test
	public void saveTaskDeleteNormalizesLegacyStaffFaceRecordToStaffPermissionTask() throws Exception {
		SmtTaskDownRecordService downRecordService = Mockito.mock(SmtTaskDownRecordService.class);
		SmtDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtDeviceTaskServiceImpl service = new SmtDeviceTaskServiceImpl(downRecordService,
				Mockito.mock(SmtIscDownRecordService.class),
				Mockito.mock(SmtDeviceAuthorityRelationMapper.class),
				deviceMapper,
				Mockito.mock(SmtIscDeviceTaskService.class));
		setField(service, "baseMapper", deviceTaskMapper);
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setDeviceName("device-1");
		device.setIsSync(StaffSyncEnum.NO.getCode());
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(0);
		SmtTaskDownRecord legacyFaceRecord = new SmtTaskDownRecord();
		legacyFaceRecord.setCardNo("1001");
		legacyFaceRecord.setDeviceCode("device-1");
		legacyFaceRecord.setDeviceType(DeviceTaskConstants.CARD);
		legacyFaceRecord.setServiceType(DeviceTaskConstants.UPDATE_FACE);
		Mockito.when(downRecordService.getOne(Mockito.any()))
				.thenReturn(null)
				.thenReturn(legacyFaceRecord);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtDeviceTask.class))).thenAnswer(invocation -> {
			SmtDeviceTask task = invocation.getArgument(0);
			task.setId(8002);
			return 1;
		});
		DeviceTaskVO taskVO = new DeviceTaskVO();
		taskVO.setAction(DeviceTaskActionEnum.DELAY_DEL.getCode());
		taskVO.setDeviceCode("device-1");
		taskVO.setCardNo("1001");
		taskVO.setDeviceType(DeviceTaskConstants.CARD);
		taskVO.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		taskVO.setStartTime(1780360200L);
		taskVO.setOverTime(1780394400L);

		String taskId = service.saveTask(taskVO);

		Assert.assertEquals("8002", taskId);
		ArgumentCaptor<LambdaQueryWrapper> downRecordQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(downRecordService, Mockito.times(2)).getOne(downRecordQueryCaptor.capture());
		assertQueryHasParam(downRecordQueryCaptor.getAllValues().get(0), DeviceTaskConstants.CARD_STAFF_IMPORT);
		assertQueryHasParam(downRecordQueryCaptor.getAllValues().get(1), DeviceTaskConstants.UPDATE_FACE);
		ArgumentCaptor<SmtDeviceTask> insertedTask = ArgumentCaptor.forClass(SmtDeviceTask.class);
		Mockito.verify(deviceTaskMapper).insert(insertedTask.capture());
		Assert.assertEquals(DeviceTaskConstants.CARD, insertedTask.getValue().getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD_STAFF_IMPORT, insertedTask.getValue().getServiceType());
		Assert.assertEquals(DeviceTaskActionEnum.DELAY_DEL.getCode(), insertedTask.getValue().getAction());
	}

	/**
	 * 车辆设备类型常量对应的重复任务不应再次创建。
	 */
	@Test
	public void saveTaskDeduplicatesCarDeviceTypeConstantTasks() throws Exception {
		SmtTaskDownRecordService downRecordService = Mockito.mock(SmtTaskDownRecordService.class);
		SmtDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtDeviceTaskServiceImpl service = new SmtDeviceTaskServiceImpl(downRecordService,
				Mockito.mock(SmtIscDownRecordService.class),
				Mockito.mock(SmtDeviceAuthorityRelationMapper.class),
				deviceMapper,
				Mockito.mock(SmtIscDeviceTaskService.class));
		setField(service, "baseMapper", deviceTaskMapper);
		SmtDevice device = new SmtDevice();
		device.setId("car-device-1");
		device.setDeviceName("car-device-1");
		device.setIsSync(StaffSyncEnum.NO.getCode());
		Mockito.when(deviceMapper.selectById("car-device-1")).thenReturn(device);
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(1);
		DeviceTaskVO taskVO = new DeviceTaskVO();
		taskVO.setAction(DeviceTaskActionEnum.DOWN.getCode());
		taskVO.setCardNo("4001");
		taskVO.setDeviceCode("car-device-1");
		taskVO.setDeviceType(DeviceTaskConstants.CAR);
		taskVO.setServiceType(DeviceTaskConstants.CAT_ADMITTANCE);
		taskVO.setGeneral("粤B12345");
		taskVO.setStartTime(1780360200L);
		taskVO.setOverTime(1780394400L);

		String taskId = service.saveTask(taskVO);

		Assert.assertEquals("任务已存在", taskId);
		Mockito.verify(deviceTaskMapper).selectCount(Mockito.any());
		Mockito.verify(deviceTaskMapper, Mockito.never()).insert(Mockito.any(SmtDeviceTask.class));
	}

	/**
	 * 删除任务应按设备类型和服务类型查询对应的下发记录。
	 */
	@Test
	public void saveTaskDeleteLooksUpDownRecordByDeviceTypeAndServiceType() throws Exception {
		SmtTaskDownRecordService downRecordService = Mockito.mock(SmtTaskDownRecordService.class);
		SmtDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtDeviceTaskServiceImpl service = new SmtDeviceTaskServiceImpl(downRecordService,
				Mockito.mock(SmtIscDownRecordService.class),
				Mockito.mock(SmtDeviceAuthorityRelationMapper.class),
				deviceMapper,
				Mockito.mock(SmtIscDeviceTaskService.class));
		setField(service, "baseMapper", deviceTaskMapper);
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setDeviceName("device-1");
		device.setIsSync(StaffSyncEnum.NO.getCode());
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);
		SmtTaskDownRecord downRecord = new SmtTaskDownRecord();
		downRecord.setId(7001);
		downRecord.setCardNo("3001");
		downRecord.setDeviceCode("device-1");
		downRecord.setDeviceType(DeviceTaskConstants.CARD);
		downRecord.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
		Mockito.when(downRecordService.getOne(Mockito.any())).thenReturn(downRecord);
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtDeviceTask.class))).thenReturn(1);
		DeviceTaskVO taskVO = new DeviceTaskVO();
		taskVO.setAction(DeviceTaskActionEnum.DEL.getCode());
		taskVO.setCardNo("3001");
		taskVO.setDeviceCode("device-1");
		taskVO.setDeviceType(DeviceTaskConstants.CARD);
		taskVO.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
		taskVO.setStartTime(1780360200L);
		taskVO.setOverTime(1780394400L);

		service.saveTask(taskVO);

		ArgumentCaptor<LambdaQueryWrapper> downRecordQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(downRecordService).getOne(downRecordQueryCaptor.capture());
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD);
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD_ADMITTANCE);
		ArgumentCaptor<SmtDeviceTask> insertedDeleteTask = ArgumentCaptor.forClass(SmtDeviceTask.class);
		Mockito.verify(deviceTaskMapper).insert(insertedDeleteTask.capture());
		Assert.assertEquals(DeviceTaskConstants.CARD, insertedDeleteTask.getValue().getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD_ADMITTANCE, insertedDeleteTask.getValue().getServiceType());
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), insertedDeleteTask.getValue().getAction());
	}

	/**
	 * 断言 Lambda 查询参数中包含预期值。
	 */
	private void assertQueryHasParam(LambdaQueryWrapper queryWrapper, Object expected) {
		queryWrapper.getSqlSegment();
		Assert.assertTrue(queryWrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(expected).equals(String.valueOf(value))));
	}

	/**
	 * 构造用于批量删除任务的非 ISC 设备下发记录。
	 */
	private SmtTaskDownRecord newDownRecord(String deviceCode) {
		SmtTaskDownRecord downRecord = new SmtTaskDownRecord();
		downRecord.setCardNo("1001");
		downRecord.setDeviceCode(deviceCode);
		downRecord.setDeviceType(DeviceTaskConstants.CARD);
		downRecord.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		return downRecord;
	}

	/**
	 * 为测试对象及其父类的指定字段注入 Mock。
	 */
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
		throw new NoSuchFieldException(name + " on " + target.getClass());
	}
}
