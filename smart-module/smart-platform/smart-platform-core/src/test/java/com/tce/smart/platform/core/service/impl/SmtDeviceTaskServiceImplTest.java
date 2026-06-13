package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
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
import java.util.Collections;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtDeviceTaskServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtTaskDownRecord.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtDeviceTask.class);
	}

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

	private void assertQueryHasParam(LambdaQueryWrapper queryWrapper, Object expected) {
		queryWrapper.getSqlSegment();
		Assert.assertTrue(queryWrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(expected).equals(String.valueOf(value))));
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
		throw new NoSuchFieldException(name + " on " + target.getClass());
	}
}
