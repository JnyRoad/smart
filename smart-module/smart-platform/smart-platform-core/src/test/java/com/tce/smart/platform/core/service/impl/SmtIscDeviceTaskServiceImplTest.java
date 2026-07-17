package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.mapper.SmtAdmittanceFellowMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtFellowVisitorMapper;
import com.tce.smart.platform.core.mapper.SmtIscDeviceTaskMapper;
import com.tce.smart.platform.core.mapper.SmtVisitorMapper;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.platform.core.enums.ISCDeviceTaskEnum;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtIscDeviceTaskServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDownRecord.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDeviceTask.class);
	}

	@Test
	public void saveSecurityAuthTaskCancelsOldInitTaskWithCasAndCreatesLatestBatchTask() throws Exception {
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(Mockito.mock(SmtIscDownRecordService.class),
				deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class), deviceMapper);
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setParkId(5000021);
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);
		LocalDateTime oldUpdateTime = LocalDateTime.of(2026, 7, 16, 20, 0);
		SmtIscDeviceTask oldTask = securityAuthTask(81L, 1001L, 9001L,
				DeviceTaskStatusEnum.INIT.getCode(), oldUpdateTime, null);
		Mockito.when(deviceTaskMapper.listSecurityAuthTasksByIntent(1001L,
				"SECURITY_AUTH:11:22:device-1")).thenReturn(Collections.singletonList(oldTask));
		Mockito.when(deviceTaskMapper.cancelSecurityAuthTask(Mockito.eq(81L),
				Mockito.eq(DeviceTaskStatusEnum.INIT.getCode()), Mockito.eq(9001L), Mockito.eq(oldUpdateTime),
				Mockito.eq(DeviceTaskStatusEnum.CANCEL.getCode()), Mockito.eq(9002L),
				Mockito.any(LocalDateTime.class))).thenReturn(1);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtIscDeviceTask.class))).thenAnswer(invocation -> {
			SmtIscDeviceTask task = invocation.getArgument(0);
			task.setId(82L);
			return 1;
		});

		String taskId = service.saveSecurityAuthTask(securityAuthTaskVO(1001L, 101L, 9002L));

		Assert.assertEquals("82", taskId);
		Mockito.verify(deviceTaskMapper).cancelSecurityAuthTask(Mockito.eq(81L),
				Mockito.eq(DeviceTaskStatusEnum.INIT.getCode()), Mockito.eq(9001L), Mockito.eq(oldUpdateTime),
				Mockito.eq(DeviceTaskStatusEnum.CANCEL.getCode()), Mockito.eq(9002L),
				Mockito.any(LocalDateTime.class));
		ArgumentCaptor<SmtIscDeviceTask> insertedTask = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(deviceTaskMapper).insert(insertedTask.capture());
		Assert.assertEquals("SECURITY_AUTH", insertedTask.getValue().getSourceType());
		Assert.assertEquals(Long.valueOf(1001L), insertedTask.getValue().getSourceId());
		Assert.assertEquals(Long.valueOf(101L), insertedTask.getValue().getSourceDetailId());
		Assert.assertEquals(Long.valueOf(9002L), insertedTask.getValue().getBatchId());
		Assert.assertEquals("SECURITY_AUTH:11:22:device-1", insertedTask.getValue().getIntentKey());
	}

	@Test
	public void saveSecurityAuthTaskDoesNotOverwriteTaskChangedByAnotherThread() throws Exception {
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(Mockito.mock(SmtIscDownRecordService.class),
				deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class), deviceMapper);
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setParkId(5000021);
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);
		LocalDateTime oldUpdateTime = LocalDateTime.of(2026, 7, 16, 20, 0);
		Mockito.when(deviceTaskMapper.listSecurityAuthTasksByIntent(Mockito.anyLong(), Mockito.anyString()))
				.thenReturn(Collections.singletonList(securityAuthTask(81L, 1001L, 9001L,
						DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode(), oldUpdateTime, null)));
		Mockito.when(deviceTaskMapper.cancelSecurityAuthTask(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyLong(),
				Mockito.any(LocalDateTime.class), Mockito.anyInt(), Mockito.anyLong(),
				Mockito.any(LocalDateTime.class))).thenReturn(0);

		try {
			service.saveSecurityAuthTask(securityAuthTaskVO(1001L, 101L, 9002L));
			Assert.fail("CAS 未命中时必须中止本轮创建并由事务重试");
		} catch (IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage().contains("状态已变化"));
		}
		Mockito.verify(deviceTaskMapper, Mockito.never()).insert(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void saveSecurityAuthTaskProtectsFreshDoingTaskWithoutIscTaskId() throws Exception {
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(Mockito.mock(SmtIscDownRecordService.class),
				deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class), deviceMapper);
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setParkId(5000021);
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);
		Mockito.when(deviceTaskMapper.listSecurityAuthTasksByIntent(Mockito.anyLong(), Mockito.anyString()))
				.thenReturn(Collections.singletonList(securityAuthTask(81L, 1001L, 9001L,
						DeviceTaskStatusEnum.DOING.getCode(), LocalDateTime.now().minusSeconds(30), null)));

		try {
			service.saveSecurityAuthTask(securityAuthTaskVO(1001L, 101L, 9002L));
			Assert.fail("提交保护窗口内不得接管空 ISC_TASK_ID 的 DOING 任务");
		} catch (IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage().contains("保护窗口"));
		}
		Mockito.verify(deviceTaskMapper, Mockito.never()).cancelSecurityAuthTask(Mockito.anyLong(), Mockito.anyInt(),
				Mockito.anyLong(), Mockito.any(), Mockito.anyInt(), Mockito.anyLong(), Mockito.any());
		Mockito.verify(deviceTaskMapper, Mockito.never()).insert(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void saveSecurityAuthTaskTakesOverStaleDoingTaskWithoutIscTaskId() throws Exception {
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(Mockito.mock(SmtIscDownRecordService.class),
				deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class), deviceMapper);
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setParkId(5000021);
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);
		LocalDateTime staleUpdateTime = LocalDateTime.now().minusMinutes(3);
		Mockito.when(deviceTaskMapper.listSecurityAuthTasksByIntent(Mockito.anyLong(), Mockito.anyString()))
				.thenReturn(Collections.singletonList(securityAuthTask(81L, 1001L, 9001L,
						DeviceTaskStatusEnum.DOING.getCode(), staleUpdateTime, null)));
		Mockito.when(deviceTaskMapper.cancelSecurityAuthTask(Mockito.eq(81L),
				Mockito.eq(DeviceTaskStatusEnum.DOING.getCode()), Mockito.eq(9001L), Mockito.eq(staleUpdateTime),
				Mockito.eq(DeviceTaskStatusEnum.CANCEL.getCode()), Mockito.eq(9002L),
				Mockito.any(LocalDateTime.class))).thenReturn(1);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtIscDeviceTask.class))).thenAnswer(invocation -> {
			SmtIscDeviceTask task = invocation.getArgument(0);
			task.setId(82L);
			return 1;
		});

		Assert.assertEquals("82", service.saveSecurityAuthTask(securityAuthTaskVO(1001L, 101L, 9002L)));
		Mockito.verify(deviceTaskMapper).cancelSecurityAuthTask(Mockito.eq(81L),
				Mockito.eq(DeviceTaskStatusEnum.DOING.getCode()), Mockito.eq(9001L), Mockito.eq(staleUpdateTime),
				Mockito.eq(DeviceTaskStatusEnum.CANCEL.getCode()), Mockito.eq(9002L),
				Mockito.any(LocalDateTime.class));
	}

	@Test
	public void saveSecurityAuthTaskLocallyTakesOverSubmittedDoingTaskAndKeepsIscAuditId() throws Exception {
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(Mockito.mock(SmtIscDownRecordService.class),
				deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class), deviceMapper);
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setParkId(5000021);
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);
		LocalDateTime oldUpdateTime = LocalDateTime.now();
		SmtIscDeviceTask oldTask = securityAuthTask(81L, 1001L, 9001L,
				DeviceTaskStatusEnum.DOING.getCode(), oldUpdateTime, "isc-audit-81");
		Mockito.when(deviceTaskMapper.listSecurityAuthTasksByIntent(Mockito.anyLong(), Mockito.anyString()))
				.thenReturn(Collections.singletonList(oldTask));
		Mockito.when(deviceTaskMapper.cancelSecurityAuthTask(Mockito.eq(81L),
				Mockito.eq(DeviceTaskStatusEnum.DOING.getCode()), Mockito.eq(9001L), Mockito.eq(oldUpdateTime),
				Mockito.eq(DeviceTaskStatusEnum.CANCEL.getCode()), Mockito.eq(9002L),
				Mockito.any(LocalDateTime.class))).thenReturn(1);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtIscDeviceTask.class))).thenAnswer(invocation -> {
			SmtIscDeviceTask task = invocation.getArgument(0);
			task.setId(82L);
			return 1;
		});

		service.saveSecurityAuthTask(securityAuthTaskVO(1001L, 101L, 9002L));

		Assert.assertEquals("调用方对象中的审计 ID 不得被清空", "isc-audit-81", oldTask.getIscTaskId());
		Mockito.verify(deviceTaskMapper).cancelSecurityAuthTask(Mockito.eq(81L),
				Mockito.eq(DeviceTaskStatusEnum.DOING.getCode()), Mockito.eq(9001L), Mockito.eq(oldUpdateTime),
				Mockito.eq(DeviceTaskStatusEnum.CANCEL.getCode()), Mockito.eq(9002L),
				Mockito.any(LocalDateTime.class));
	}

	@Test
	public void delVisitorDeviceAuthFindsTemporaryVisitorRecordsByCertNoAndPersonId() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtVisitorMapper visitorMapper = Mockito.mock(SmtVisitorMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper, visitorMapper);

		Long localVisitorId = 1001L;
		SmtVisitor visitor = new SmtVisitor();
		visitor.setId(localVisitorId);
		visitor.setCertNo("visitor-cert-1");
		SmtIscDownRecord downRecord = new SmtIscDownRecord();
		downRecord.setDeviceCode("device-1");
		downRecord.setDeviceType(DeviceTaskConstants.CARD);
		downRecord.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		downRecord.setCardNo("999000001001");
		downRecord.setBadge("visitor-cert-1");
		downRecord.setPersonId("isc-person-1");
		Mockito.when(visitorMapper.selectById(localVisitorId)).thenReturn(visitor);
		Mockito.when(downRecordService.list(Mockito.any())).thenReturn(Collections.singletonList(downRecord));
		Mockito.when(deviceTaskMapper.selectList(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtIscDeviceTask.class))).thenReturn(1);

		service.delVisitorDeviceAuth(localVisitorId);

		ArgumentCaptor<LambdaQueryWrapper> downRecordQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(downRecordService).list(downRecordQueryCaptor.capture());
		String downRecordSql = downRecordQueryCaptor.getValue().getSqlSegment().toUpperCase();
		Assert.assertTrue(downRecordSql.contains("CARD_NO"));
		Assert.assertTrue(downRecordSql.contains("BADGE"));
		Assert.assertTrue(downRecordSql.contains("DEVICE_TYPE"));
		Assert.assertTrue(downRecordSql.contains("SERVICE_TYPE"));
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD);
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD_VISITOR);
		assertQueryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD_ADMITTANCE);

		ArgumentCaptor<LambdaQueryWrapper> taskQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(deviceTaskMapper, Mockito.times(2)).selectList(taskQueryCaptor.capture());
		String deleteTaskSql = taskQueryCaptor.getAllValues().get(0).getSqlSegment().toUpperCase();
		Assert.assertTrue(deleteTaskSql.contains("PERSON_ID"));
		Assert.assertTrue(deleteTaskSql.contains("DEVICE_TYPE"));
		Assert.assertTrue(deleteTaskSql.contains("SERVICE_TYPE"));
		assertQueryHasParam(taskQueryCaptor.getAllValues().get(0), DeviceTaskConstants.CARD);
		assertQueryHasParam(taskQueryCaptor.getAllValues().get(0), DeviceTaskConstants.CARD_VISITOR);
		String pendingDownSql = taskQueryCaptor.getAllValues().get(1).getSqlSegment().toUpperCase();
		Assert.assertTrue(pendingDownSql.contains("DEVICE_TYPE"));
		Assert.assertTrue(pendingDownSql.contains("BADGE"));
		Assert.assertTrue(pendingDownSql.contains("CARD_NO"));

		ArgumentCaptor<SmtIscDeviceTask> insertedDeleteTask = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(deviceTaskMapper).insert(insertedDeleteTask.capture());
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), insertedDeleteTask.getValue().getAction());
		Assert.assertEquals("isc-person-1", insertedDeleteTask.getValue().getPersonId());
		Assert.assertEquals("visitor-cert-1", insertedDeleteTask.getValue().getBadge());
	}

	@Test
	public void saveTaskAllowsHefeiAdmittanceCardAccessTasks() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper,
				Mockito.mock(SmtVisitorMapper.class), deviceMapper);
		setField(service, "hfParkId", 5000021);
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setParkId(5000021);
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtIscDeviceTask.class))).thenAnswer(invocation -> {
			SmtIscDeviceTask task = invocation.getArgument(0);
			task.setId(123L);
			return 1;
		});
		DeviceTaskVO taskVO = new DeviceTaskVO();
		taskVO.setAction(DeviceTaskActionEnum.DOWN.getCode());
		taskVO.setDeviceType(DeviceTaskConstants.CARD);
		taskVO.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
		taskVO.setDeviceCode("device-1");
		taskVO.setCardNo("1001");
		taskVO.setStartTime(1780360200L);
		taskVO.setOverTime(1780394400L);

		String taskId = service.saveTask(taskVO);

		Assert.assertEquals("123", taskId);
		ArgumentCaptor<SmtIscDeviceTask> insertedTask = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(deviceTaskMapper).insert(insertedTask.capture());
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), insertedTask.getValue().getStatus());
		Assert.assertEquals(DeviceTaskConstants.CARD_ADMITTANCE, insertedTask.getValue().getServiceType());
	}

	@Test
	public void saveTaskAllowsHefeiStaffUpdateFaceTasks() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper,
				Mockito.mock(SmtVisitorMapper.class), deviceMapper);
		setField(service, "hfParkId", 5000021);
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setParkId(5000021);
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtIscDeviceTask.class))).thenAnswer(invocation -> {
			SmtIscDeviceTask task = invocation.getArgument(0);
			task.setId(126L);
			return 1;
		});
		DeviceTaskVO taskVO = new DeviceTaskVO();
		taskVO.setAction(DeviceTaskActionEnum.UPDATE.getCode());
		taskVO.setDeviceType(DeviceTaskConstants.CARD);
		taskVO.setServiceType(DeviceTaskConstants.UPDATE_FACE);
		taskVO.setDeviceCode("device-1");
		taskVO.setCardNo("1001");
		taskVO.setImageId("face-new");
		taskVO.setStartTime(1780360200L);
		taskVO.setOverTime(1780394400L);

		String taskId = service.saveTask(taskVO);

		Assert.assertEquals("126", taskId);
		ArgumentCaptor<SmtIscDeviceTask> insertedTask = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(deviceTaskMapper).insert(insertedTask.capture());
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), insertedTask.getValue().getStatus());
		Assert.assertEquals(DeviceTaskConstants.UPDATE_FACE, insertedTask.getValue().getServiceType());
		Assert.assertEquals("face-new", insertedTask.getValue().getImageId());
	}

	@Test
	public void saveTaskAllowsHefeiInitialStaffCardAccessTasks() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper,
				Mockito.mock(SmtVisitorMapper.class), deviceMapper);
		setField(service, "hfParkId", 5000021);
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setParkId(5000021);
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtIscDeviceTask.class))).thenAnswer(invocation -> {
			SmtIscDeviceTask task = invocation.getArgument(0);
			task.setId(127L);
			return 1;
		});
		DeviceTaskVO taskVO = new DeviceTaskVO();
		taskVO.setAction(DeviceTaskActionEnum.DOWN.getCode());
		taskVO.setDeviceType(DeviceTaskConstants.CARD);
		taskVO.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		taskVO.setDeviceCode("device-1");
		taskVO.setCardNo("1001");
		taskVO.setImageId("face-new");
		taskVO.setStartTime(1780360200L);
		taskVO.setOverTime(DeviceTaskConstants.maxTime);

		String taskId = service.saveTask(taskVO);

		Assert.assertEquals("127", taskId);
		ArgumentCaptor<SmtIscDeviceTask> insertedTask = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(deviceTaskMapper).insert(insertedTask.capture());
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), insertedTask.getValue().getStatus());
		Assert.assertEquals(DeviceTaskConstants.CARD_STAFF_IMPORT, insertedTask.getValue().getServiceType());
		Assert.assertEquals(DeviceTaskActionEnum.DOWN.getCode(), insertedTask.getValue().getAction());
	}

	@Test
	public void saveTaskRejectsHefeiUnsupportedStaffCardServiceTypes() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper,
				Mockito.mock(SmtVisitorMapper.class), deviceMapper);
		setField(service, "hfParkId", 5000021);
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setParkId(5000021);
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(0);
		DeviceTaskVO taskVO = new DeviceTaskVO();
		taskVO.setAction(DeviceTaskActionEnum.DOWN.getCode());
		taskVO.setDeviceType(DeviceTaskConstants.CARD);
		taskVO.setServiceType(DeviceTaskConstants.CARD_APP_PERFECT);
		taskVO.setDeviceCode("device-1");
		taskVO.setCardNo("1001");
		taskVO.setStartTime(1780360200L);
		taskVO.setOverTime(DeviceTaskConstants.maxTime);

		String taskId = service.saveTask(taskVO);

		Assert.assertEquals("合肥非访客人脸照片无需下发ISC权限", taskId);
		Mockito.verify(deviceTaskMapper, Mockito.never()).insert(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void saveTaskRejectsVehicleAccessTasksEvenIfLegacySwitchEnabled() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper,
				Mockito.mock(SmtVisitorMapper.class), deviceMapper);
		setField(service, "hfParkId", 5000021);
		setOptionalField(service, "iscVehicleAuthEnabled", true);
		SmtDevice device = new SmtDevice();
		device.setId("car-device-1");
		device.setParkId(5000021);
		Mockito.when(deviceMapper.selectById("car-device-1")).thenReturn(device);
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtIscDeviceTask.class))).thenAnswer(invocation -> {
			SmtIscDeviceTask task = invocation.getArgument(0);
			task.setId(124L);
			return 1;
		});
		DeviceTaskVO taskVO = new DeviceTaskVO();
		taskVO.setAction(DeviceTaskActionEnum.DOWN.getCode());
		taskVO.setDeviceType(DeviceTaskConstants.CAR);
		taskVO.setServiceType(DeviceTaskConstants.CAT_ADMITTANCE);
		taskVO.setDeviceCode("car-device-1");
		taskVO.setCardNo("4001");
		taskVO.setStartTime(1780360200L);
		taskVO.setOverTime(1780394400L);

		String taskId = service.saveTask(taskVO);

		Assert.assertEquals("ISC车辆权限不支持下发", taskId);
		Mockito.verify(deviceTaskMapper, Mockito.never()).selectCount(Mockito.any());
		Mockito.verify(deviceMapper, Mockito.never()).selectById(Mockito.anyString());
		Mockito.verify(deviceTaskMapper, Mockito.never()).insert(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void saveTaskDeleteBackfillsDownRecordTypesWhenCallerOmitsThem() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper,
				Mockito.mock(SmtVisitorMapper.class), deviceMapper);
		setField(service, "hfParkId", 0);
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setParkId(5000021);
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(0);
		SmtIscDownRecord downRecord = new SmtIscDownRecord();
		downRecord.setCardNo("1001");
		downRecord.setDeviceCode("device-1");
		downRecord.setDeviceType(DeviceTaskConstants.CARD);
		downRecord.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		Mockito.when(downRecordService.getOne(Mockito.any())).thenReturn(downRecord);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtIscDeviceTask.class))).thenAnswer(invocation -> {
			SmtIscDeviceTask task = invocation.getArgument(0);
			task.setId(125L);
			return 1;
		});
		DeviceTaskVO taskVO = new DeviceTaskVO();
		taskVO.setAction(DeviceTaskActionEnum.DEL.getCode());
		taskVO.setDeviceCode("device-1");
		taskVO.setCardNo("1001");
		taskVO.setStartTime(1780360200L);
		taskVO.setOverTime(1780394400L);

		String taskId = service.saveTask(taskVO);

		Assert.assertEquals("125", taskId);
		ArgumentCaptor<SmtIscDeviceTask> insertedTask = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(deviceTaskMapper).insert(insertedTask.capture());
		Assert.assertEquals(DeviceTaskConstants.CARD, insertedTask.getValue().getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD_STAFF_IMPORT, insertedTask.getValue().getServiceType());
	}

	@Test
	public void saveTaskDeleteNormalizesLegacyStaffFaceRecordToStaffPermissionTask() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper,
				Mockito.mock(SmtVisitorMapper.class), deviceMapper);
		setField(service, "hfParkId", 0);
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setParkId(5000021);
		Mockito.when(deviceMapper.selectById("device-1")).thenReturn(device);
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(0);
		SmtIscDownRecord legacyFaceRecord = new SmtIscDownRecord();
		legacyFaceRecord.setCardNo("1001");
		legacyFaceRecord.setDeviceCode("device-1");
		legacyFaceRecord.setDeviceType(DeviceTaskConstants.CARD);
		legacyFaceRecord.setServiceType(DeviceTaskConstants.UPDATE_FACE);
		legacyFaceRecord.setPersonId("isc-person-1");
		Mockito.when(downRecordService.getOne(Mockito.any()))
				.thenReturn(null)
				.thenReturn(legacyFaceRecord);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtIscDeviceTask.class))).thenAnswer(invocation -> {
			SmtIscDeviceTask task = invocation.getArgument(0);
			task.setId(128L);
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

		Assert.assertEquals("128", taskId);
		ArgumentCaptor<LambdaQueryWrapper> downRecordQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(downRecordService, Mockito.times(2)).getOne(downRecordQueryCaptor.capture());
		assertQueryHasParam(downRecordQueryCaptor.getAllValues().get(0), DeviceTaskConstants.CARD_STAFF_IMPORT);
		assertQueryHasParam(downRecordQueryCaptor.getAllValues().get(1), DeviceTaskConstants.UPDATE_FACE);
		ArgumentCaptor<SmtIscDeviceTask> insertedTask = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(deviceTaskMapper).insert(insertedTask.capture());
		Assert.assertEquals(DeviceTaskConstants.CARD, insertedTask.getValue().getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD_STAFF_IMPORT, insertedTask.getValue().getServiceType());
		Assert.assertEquals(DeviceTaskActionEnum.DELAY_DEL.getCode(), insertedTask.getValue().getAction());
		Assert.assertEquals("isc-person-1", insertedTask.getValue().getPersonId());
	}

	@Test
	public void saveTaskRejectsVehicleTasksBeforeDeduplicationEvenIfLegacySwitchEnabled() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper,
				Mockito.mock(SmtVisitorMapper.class), deviceMapper);
		setField(service, "hfParkId", 5000021);
		setOptionalField(service, "iscVehicleAuthEnabled", true);
		SmtDevice device = new SmtDevice();
		device.setId("car-device-1");
		device.setParkId(5000021);
		Mockito.when(deviceMapper.selectById("car-device-1")).thenReturn(device);
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(1);
		DeviceTaskVO taskVO = new DeviceTaskVO();
		taskVO.setAction(DeviceTaskActionEnum.DOWN.getCode());
		taskVO.setDeviceType(DeviceTaskConstants.CAR);
		taskVO.setServiceType(DeviceTaskConstants.CAT_ADMITTANCE);
		taskVO.setDeviceCode("car-device-1");
		taskVO.setCardNo("4001");
		taskVO.setGeneral("粤B12345");
		taskVO.setStartTime(1780360200L);
		taskVO.setOverTime(1780394400L);

		String taskId = service.saveTask(taskVO);

		Assert.assertEquals("ISC车辆权限不支持下发", taskId);
		Mockito.verify(deviceTaskMapper, Mockito.never()).selectCount(Mockito.any());
		Mockito.verify(deviceTaskMapper, Mockito.never()).insert(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void delVisitorDeviceAuthCreatesNewDeleteTaskWhenOnlyHistoricalDeleteTaskExists() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtVisitorMapper visitorMapper = Mockito.mock(SmtVisitorMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper, visitorMapper);
		Long localVisitorId = 1002L;
		SmtVisitor visitor = new SmtVisitor();
		visitor.setId(localVisitorId);
		visitor.setCertNo("visitor-cert-2");
		SmtIscDownRecord downRecord = new SmtIscDownRecord();
		downRecord.setDeviceCode("device-2");
		downRecord.setDeviceType(DeviceTaskConstants.CARD);
		downRecord.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		downRecord.setCardNo("999000001002");
		downRecord.setBadge("visitor-cert-2");
		downRecord.setPersonId("isc-person-2");
		SmtIscDeviceTask historicalDeleteTask = new SmtIscDeviceTask();
		historicalDeleteTask.setId(9001L);
		historicalDeleteTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		historicalDeleteTask.setStatus(DeviceTaskStatusEnum.SUCCESS.getCode());
		historicalDeleteTask.setDeviceCode("device-2");
		historicalDeleteTask.setPersonId("isc-person-2");
		historicalDeleteTask.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		Mockito.when(visitorMapper.selectById(localVisitorId)).thenReturn(visitor);
		Mockito.when(downRecordService.list(Mockito.any())).thenReturn(Collections.singletonList(downRecord));
		Mockito.when(deviceTaskMapper.selectList(Mockito.any()))
				.thenReturn(Collections.singletonList(historicalDeleteTask))
				.thenReturn(Collections.emptyList());
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtIscDeviceTask.class))).thenReturn(1);

		service.delVisitorDeviceAuth(localVisitorId);

		Mockito.verify(deviceTaskMapper, Mockito.never()).updateById(Mockito.eq(historicalDeleteTask));
		ArgumentCaptor<SmtIscDeviceTask> insertedDeleteTask = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(deviceTaskMapper).insert(insertedDeleteTask.capture());
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), insertedDeleteTask.getValue().getAction());
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), insertedDeleteTask.getValue().getStatus());
		Assert.assertEquals("isc-person-2", insertedDeleteTask.getValue().getPersonId());
	}

	@Test
	public void delVisitorDeviceAuthReusesDoingDeleteTask() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtVisitorMapper visitorMapper = Mockito.mock(SmtVisitorMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper, visitorMapper);
		Long localVisitorId = 1003L;
		SmtVisitor visitor = new SmtVisitor();
		visitor.setId(localVisitorId);
		visitor.setCertNo("visitor-cert-3");
		SmtIscDownRecord downRecord = new SmtIscDownRecord();
		downRecord.setDeviceCode("device-3");
		downRecord.setDeviceType(DeviceTaskConstants.CARD);
		downRecord.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		downRecord.setCardNo("999000001003");
		downRecord.setBadge("visitor-cert-3");
		downRecord.setPersonId("isc-person-3");
		SmtIscDeviceTask doingDeleteTask = new SmtIscDeviceTask();
		doingDeleteTask.setId(9003L);
		doingDeleteTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		doingDeleteTask.setStatus(DeviceTaskStatusEnum.DOING.getCode());
		doingDeleteTask.setCode(202);
		doingDeleteTask.setIscTaskId("download-task-3");
		doingDeleteTask.setDeviceCode("device-3");
		doingDeleteTask.setPersonId("isc-person-3");
		doingDeleteTask.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		Mockito.when(visitorMapper.selectById(localVisitorId)).thenReturn(visitor);
		Mockito.when(downRecordService.list(Mockito.any())).thenReturn(Collections.singletonList(downRecord));
		Mockito.when(deviceTaskMapper.selectList(Mockito.any()))
				.thenReturn(Collections.singletonList(doingDeleteTask))
				.thenReturn(Collections.emptyList());

		service.delVisitorDeviceAuth(localVisitorId);

		ArgumentCaptor<LambdaQueryWrapper> taskQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(deviceTaskMapper, Mockito.times(2)).selectList(taskQueryCaptor.capture());
		assertQueryHasParam(taskQueryCaptor.getAllValues().get(0), DeviceTaskConstants.CARD);
		assertQueryHasParam(taskQueryCaptor.getAllValues().get(0), DeviceTaskConstants.CARD_VISITOR);
		Mockito.verify(deviceTaskMapper).updateById(doingDeleteTask);
		Mockito.verify(deviceTaskMapper, Mockito.never()).insert(Mockito.any(SmtIscDeviceTask.class));
		Assert.assertEquals(DeviceTaskStatusEnum.DOING.getCode(), doingDeleteTask.getStatus());
		Assert.assertEquals(Integer.valueOf(202), doingDeleteTask.getCode());
		Assert.assertEquals("download-task-3", doingDeleteTask.getIscTaskId());
	}

	@Test
	public void delVisitorDeviceAuthDoesNotReuseDeleteTaskAfterMaxRetryTimes() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtVisitorMapper visitorMapper = Mockito.mock(SmtVisitorMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper, visitorMapper);
		Long localVisitorId = 1004L;
		SmtVisitor visitor = new SmtVisitor();
		visitor.setId(localVisitorId);
		visitor.setCertNo("visitor-cert-4");
		SmtIscDownRecord downRecord = new SmtIscDownRecord();
		downRecord.setDeviceCode("device-4");
		downRecord.setDeviceType(DeviceTaskConstants.CARD);
		downRecord.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		downRecord.setCardNo("999000001004");
		downRecord.setBadge("visitor-cert-4");
		downRecord.setPersonId("isc-person-4");
		SmtIscDeviceTask retryExceededTask = new SmtIscDeviceTask();
		retryExceededTask.setId(9004L);
		retryExceededTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		retryExceededTask.setStatus(DeviceTaskStatusEnum.FAIL.getCode());
		retryExceededTask.setTimes(DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES);
		retryExceededTask.setCode(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode());
		retryExceededTask.setRemark("请人工介入处理");
		retryExceededTask.setDeviceCode("device-4");
		retryExceededTask.setPersonId("isc-person-4");
		retryExceededTask.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		Mockito.when(visitorMapper.selectById(localVisitorId)).thenReturn(visitor);
		Mockito.when(downRecordService.list(Mockito.any())).thenReturn(Collections.singletonList(downRecord));
		Mockito.when(deviceTaskMapper.selectList(Mockito.any()))
				.thenReturn(Collections.singletonList(retryExceededTask))
				.thenReturn(Collections.emptyList());
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtIscDeviceTask.class))).thenReturn(1);

		service.delVisitorDeviceAuth(localVisitorId);

		Mockito.verify(deviceTaskMapper, Mockito.never()).updateById(Mockito.eq(retryExceededTask));
		ArgumentCaptor<SmtIscDeviceTask> insertedDeleteTask = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(deviceTaskMapper).insert(insertedDeleteTask.capture());
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), insertedDeleteTask.getValue().getAction());
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), insertedDeleteTask.getValue().getStatus());
		Assert.assertEquals("isc-person-4", insertedDeleteTask.getValue().getPersonId());
		Assert.assertEquals(DeviceTaskStatusEnum.FAIL.getCode(), retryExceededTask.getStatus());
		Assert.assertEquals(DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES, retryExceededTask.getTimes());
		Assert.assertEquals("请人工介入处理", retryExceededTask.getRemark());
	}

	@Test
	public void stopExceededRetryAuthTasksIncludesOfflineTasks() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class));
		Mockito.when(deviceTaskMapper.update(Mockito.any(), Mockito.any())).thenReturn(1);

		boolean updated = service.stopExceededRetryAuthTasks(DeviceTaskConstants.CARD,
				DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES, "请人工介入处理");

		Assert.assertTrue(updated);
		ArgumentCaptor<LambdaUpdateWrapper> wrapperCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(deviceTaskMapper).update(Mockito.isNull(), wrapperCaptor.capture());
		LambdaUpdateWrapper wrapper = wrapperCaptor.getValue();
		String sqlSegment = wrapper.getSqlSegment().toUpperCase();
		Assert.assertTrue(sqlSegment.contains("STATUS"));
		Assert.assertTrue(wrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode()).equals(String.valueOf(value))));
		Assert.assertTrue(wrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES).equals(String.valueOf(value))));
	}

	@Test
	public void requeueOrphanDoingTasksTargetsOnlyOrphanDoingState() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class));
		Mockito.when(deviceTaskMapper.update(Mockito.any(), Mockito.any())).thenReturn(1);

		int requeued = service.requeueOrphanDoingTasks(DeviceTaskConstants.CARD, 30);

		Assert.assertTrue(requeued > 0);
		ArgumentCaptor<LambdaUpdateWrapper> wrapperCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(deviceTaskMapper).update(Mockito.isNull(), wrapperCaptor.capture());
		LambdaUpdateWrapper wrapper = wrapperCaptor.getValue();
		String sqlSegment = wrapper.getSqlSegment().toUpperCase();
		Assert.assertTrue(sqlSegment.contains("STATUS"));
		Assert.assertTrue(sqlSegment.contains("ISC_TASK_ID IS NULL"));
		// 正常轮询中的任务（code=201/202 且 iscTaskId 非空）不能被回收
		Assert.assertTrue(wrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(ISCDeviceTaskEnum.AUTH_CONFIG_OK.getCode()).equals(String.valueOf(value))));
		Assert.assertTrue(wrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_OK.getCode()).equals(String.valueOf(value))));
		Assert.assertTrue(wrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(DeviceTaskStatusEnum.DOING.getCode()).equals(String.valueOf(value))));
	}

	@Test
	public void expireOverdueDownloadTasksSkipsDeleteActions() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class));
		Mockito.when(deviceTaskMapper.update(Mockito.any(), Mockito.any())).thenReturn(2);

		int expired = service.expireOverdueDownloadTasks(DeviceTaskConstants.CARD);

		Assert.assertEquals(2, expired);
		ArgumentCaptor<LambdaUpdateWrapper> wrapperCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(deviceTaskMapper).update(Mockito.isNull(), wrapperCaptor.capture());
		LambdaUpdateWrapper wrapper = wrapperCaptor.getValue();
		String sqlSegment = wrapper.getSqlSegment().toUpperCase();
		Assert.assertTrue(sqlSegment.contains("OVER_TIME"));
		Assert.assertTrue(sqlSegment.contains("ACTION"));
		java.util.Collection<Object> params = wrapper.getParamNameValuePairs().values();
		Assert.assertTrue(params.stream().anyMatch(value ->
				String.valueOf(DeviceTaskStatusEnum.EXPIRED.getCode()).equals(String.valueOf(value))));
		// 下发/更新/延迟下发/延迟更新在过期范围内
		for (Integer action : new Integer[]{DeviceTaskActionEnum.DOWN.getCode(), DeviceTaskActionEnum.UPDATE.getCode(),
				DeviceTaskActionEnum.DELAY_DOWN.getCode(), DeviceTaskActionEnum.DELAY_UPDATE.getCode()}) {
			Assert.assertTrue("missing action " + action,
					params.stream().anyMatch(value -> String.valueOf(action).equals(String.valueOf(value))));
		}
		// 删除类任务（2/12）以OVER_TIME为执行时间，不得被过期收敛
		Assert.assertFalse(params.stream().anyMatch(value ->
				String.valueOf(DeviceTaskActionEnum.DEL.getCode()).equals(String.valueOf(value))));
		Assert.assertFalse(params.stream().anyMatch(value ->
				String.valueOf(DeviceTaskActionEnum.DELAY_DEL.getCode()).equals(String.valueOf(value))));
	}

	@Test
	public void requeueOrphanDoingTasksRejectsInvalidThreshold() throws Exception {
		SmtIscDeviceTaskServiceImpl service = service(Mockito.mock(SmtIscDownRecordService.class),
				Mockito.mock(SmtIscDeviceTaskMapper.class), Mockito.mock(SmtVisitorMapper.class));
		Assert.assertEquals(0, service.requeueOrphanDoingTasks(DeviceTaskConstants.CARD, 0));
	}

	@Test
	public void saveTaskRejectsVehicleDeleteTasksEvenIfLegacySwitchEnabled() throws Exception {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(downRecordService, deviceTaskMapper,
				Mockito.mock(SmtVisitorMapper.class), deviceMapper);
		setField(service, "hfParkId", 5000021);
		setOptionalField(service, "iscVehicleAuthEnabled", true);
		SmtDevice device = new SmtDevice();
		device.setId("car-device-1");
		device.setParkId(5000021);
		Mockito.when(deviceMapper.selectById("car-device-1")).thenReturn(device);
		Mockito.when(deviceTaskMapper.selectCount(Mockito.any())).thenReturn(0);
		SmtIscDownRecord downRecord = new SmtIscDownRecord();
		downRecord.setCardNo("4001");
		downRecord.setDeviceCode("car-device-1");
		downRecord.setDeviceType(DeviceTaskConstants.CAR);
		downRecord.setServiceType(DeviceTaskConstants.CAT_ADMITTANCE);
		Mockito.when(downRecordService.getOne(Mockito.any())).thenReturn(downRecord);
		Mockito.when(deviceTaskMapper.insert(Mockito.any(SmtIscDeviceTask.class))).thenAnswer(invocation -> {
			SmtIscDeviceTask task = invocation.getArgument(0);
			task.setId(125L);
			return 1;
		});
		DeviceTaskVO taskVO = new DeviceTaskVO();
		taskVO.setAction(DeviceTaskActionEnum.DEL.getCode());
		taskVO.setCardNo("4001");
		taskVO.setDeviceCode("car-device-1");
		taskVO.setDeviceType(DeviceTaskConstants.CAR);
		taskVO.setServiceType(DeviceTaskConstants.CAT_ADMITTANCE);
		taskVO.setStartTime(1780360200L);
		taskVO.setOverTime(1780394400L);

		String taskId = service.saveTask(taskVO);

		Assert.assertEquals("ISC车辆权限不支持下发", taskId);
		Mockito.verify(downRecordService, Mockito.never()).getOne(Mockito.any());
		Mockito.verify(deviceTaskMapper, Mockito.never()).insert(Mockito.any(SmtIscDeviceTask.class));
	}

	// ========== 批量终态方法的AndCollectApplyIds重载：UPDATE前先查受影响申请单ID ==========

	@Test
	public void expireOverdueDownloadTasksAndCollectApplyIdsSelectsDistinctApplyIdsWithSameCondition() throws Exception {
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(Mockito.mock(SmtIscDownRecordService.class),
				deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class));
		SmtIscDeviceTask taskA = new SmtIscDeviceTask();
		taskA.setApplyId(11L);
		SmtIscDeviceTask taskB = new SmtIscDeviceTask();
		taskB.setApplyId(11L);
		SmtIscDeviceTask taskC = new SmtIscDeviceTask();
		taskC.setApplyId(12L);
		Mockito.when(deviceTaskMapper.selectList(Mockito.any())).thenReturn(java.util.Arrays.asList(taskA, taskB, taskC));
		Mockito.when(deviceTaskMapper.update(Mockito.any(), Mockito.any())).thenReturn(3);

		java.util.Set<Long> applyIds = service.expireOverdueDownloadTasksAndCollectApplyIds(DeviceTaskConstants.CARD);

		// 返回去重后的受影响申请单ID
		Assert.assertEquals(new java.util.HashSet<>(java.util.Arrays.asList(11L, 12L)), applyIds);
		// SELECT与UPDATE共用同一份条件构造（applyExpireOverdueCondition）：SELECT额外追加APPLY_ID非空过滤
		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(deviceTaskMapper).selectList(queryCaptor.capture());
		String querySql = queryCaptor.getValue().getSqlSegment().toUpperCase();
		Assert.assertTrue(querySql.contains("APPLY_ID IS NOT NULL"));
		Assert.assertTrue(querySql.contains("OVER_TIME"));
		Assert.assertTrue(querySql.contains("ACTION"));
		// SELECT之后原UPDATE照常执行，条件同源
		ArgumentCaptor<LambdaUpdateWrapper> updateCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(deviceTaskMapper).update(Mockito.isNull(), updateCaptor.capture());
		String updateSql = updateCaptor.getValue().getSqlSegment().toUpperCase();
		Assert.assertTrue(updateSql.contains("OVER_TIME"));
		Assert.assertTrue(updateSql.contains("ACTION"));
	}

	@Test
	public void expireOverdueDownloadTasksAndCollectApplyIdsReturnsEmptyWhenNoRowsMatch() throws Exception {
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(Mockito.mock(SmtIscDownRecordService.class),
				deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class));
		Mockito.when(deviceTaskMapper.selectList(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(deviceTaskMapper.update(Mockito.any(), Mockito.any())).thenReturn(0);

		java.util.Set<Long> applyIds = service.expireOverdueDownloadTasksAndCollectApplyIds(DeviceTaskConstants.CARD);

		Assert.assertTrue(applyIds.isEmpty());
	}

	@Test
	public void stopExceededRetryAuthTasksAndCollectApplyIdsSelectsApplyIdsWithSameCondition() throws Exception {
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(Mockito.mock(SmtIscDownRecordService.class),
				deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class));
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setApplyId(21L);
		Mockito.when(deviceTaskMapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(deviceTaskMapper.update(Mockito.any(), Mockito.any())).thenReturn(1);

		java.util.Set<Long> applyIds = service.stopExceededRetryAuthTasksAndCollectApplyIds(DeviceTaskConstants.CARD,
				DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES, "请人工介入处理");

		Assert.assertEquals(Collections.singleton(21L), applyIds);
		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(deviceTaskMapper).selectList(queryCaptor.capture());
		String querySql = queryCaptor.getValue().getSqlSegment().toUpperCase();
		Assert.assertTrue(querySql.contains("APPLY_ID IS NOT NULL"));
		Assert.assertTrue(querySql.contains("TIMES"));
		// 条件与UPDATE同源（applyStopExceededRetryCondition）：最大重试次数参数出现在SELECT条件中
		Assert.assertTrue(queryCaptor.getValue().getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES).equals(String.valueOf(value))));
		Mockito.verify(deviceTaskMapper).update(Mockito.isNull(), Mockito.any(LambdaUpdateWrapper.class));
	}

	@Test
	public void stopExceededRetryAuthTasksAndCollectApplyIdsReturnsEmptyWhenNoRowsMatch() throws Exception {
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(Mockito.mock(SmtIscDownRecordService.class),
				deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class));
		Mockito.when(deviceTaskMapper.selectList(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(deviceTaskMapper.update(Mockito.any(), Mockito.any())).thenReturn(0);

		java.util.Set<Long> applyIds = service.stopExceededRetryAuthTasksAndCollectApplyIds(DeviceTaskConstants.CARD,
				DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES, "请人工介入处理");

		Assert.assertTrue(applyIds.isEmpty());
	}

	@Test
	public void markOfflineDeviceTasksAndCollectApplyIdsQueriesApplyIdsBeforeUpdate() throws Exception {
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(Mockito.mock(SmtIscDownRecordService.class),
				deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class));
		Mockito.when(deviceTaskMapper.getOfflineDeviceTaskApplyIds(DeviceTaskConstants.CARD))
				.thenReturn(new java.util.LinkedHashSet<>(java.util.Arrays.asList(31L, 32L)));
		Mockito.when(deviceTaskMapper.markOfflineDeviceTasks(Mockito.eq(DeviceTaskConstants.CARD), Mockito.any()))
				.thenReturn(2);

		java.util.Set<Long> applyIds = service.markOfflineDeviceTasksAndCollectApplyIds(DeviceTaskConstants.CARD);

		Assert.assertEquals(new java.util.HashSet<>(java.util.Arrays.asList(31L, 32L)), applyIds);
		// SELECT必须先于UPDATE（条件由XML共享片段mark_offline_device_tasks_condition保证一致）
		org.mockito.InOrder inOrder = Mockito.inOrder(deviceTaskMapper);
		inOrder.verify(deviceTaskMapper).getOfflineDeviceTaskApplyIds(DeviceTaskConstants.CARD);
		inOrder.verify(deviceTaskMapper).markOfflineDeviceTasks(Mockito.eq(DeviceTaskConstants.CARD), Mockito.any());
	}

	@Test
	public void markOfflineDeviceTasksAndCollectApplyIdsReturnsEmptyWhenNoRowsMatch() throws Exception {
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(Mockito.mock(SmtIscDownRecordService.class),
				deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class));
		// mapper无匹配行时可能返回null（MyBatis空结果的Set默认返回空集合，防御null也一并覆盖）
		Mockito.when(deviceTaskMapper.getOfflineDeviceTaskApplyIds(DeviceTaskConstants.CARD)).thenReturn(null);
		Mockito.when(deviceTaskMapper.markOfflineDeviceTasks(Mockito.eq(DeviceTaskConstants.CARD), Mockito.any()))
				.thenReturn(0);

		java.util.Set<Long> applyIds = service.markOfflineDeviceTasksAndCollectApplyIds(DeviceTaskConstants.CARD);

		Assert.assertTrue(applyIds.isEmpty());
	}

	@Test
	public void cancelStaleOfflineDownloadTasksAndCollectApplyIdsCollectsOnlyUpdatedRowsWithApplyId() throws Exception {
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(Mockito.mock(SmtIscDownRecordService.class),
				deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class));
		SmtIscDeviceTask taskWithApply = new SmtIscDeviceTask();
		taskWithApply.setId(9101L);
		taskWithApply.setApplyId(41L);
		SmtIscDeviceTask taskWithoutApply = new SmtIscDeviceTask();
		taskWithoutApply.setId(9102L);
		SmtIscDeviceTask taskUpdateMissed = new SmtIscDeviceTask();
		taskUpdateMissed.setId(9103L);
		taskUpdateMissed.setApplyId(42L);
		Mockito.when(deviceTaskMapper.getStaleOfflineDownloadTasks(DeviceTaskConstants.CARD, 3))
				.thenReturn(java.util.Arrays.asList(taskWithApply, taskWithoutApply, taskUpdateMissed));
		// 9103竞态未命中（UPDATE返回0行）：其applyId不应被收集，避免对未真正变更的申请单触发无意义聚合
		Mockito.when(deviceTaskMapper.cancelStaleOfflineDownloadTask(Mockito.eq(9101L), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(1);
		Mockito.when(deviceTaskMapper.cancelStaleOfflineDownloadTask(Mockito.eq(9102L), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(1);
		Mockito.when(deviceTaskMapper.cancelStaleOfflineDownloadTask(Mockito.eq(9103L), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(0);

		java.util.Set<Long> applyIds = service.cancelStaleOfflineDownloadTasksAndCollectApplyIds(DeviceTaskConstants.CARD, 3);

		Assert.assertEquals(Collections.singleton(41L), applyIds);
	}

	@Test
	public void cancelStaleOfflineDownloadTasksAndCollectApplyIdsReturnsEmptyWhenNoRowsMatch() throws Exception {
		SmtIscDeviceTaskMapper deviceTaskMapper = Mockito.mock(SmtIscDeviceTaskMapper.class);
		SmtIscDeviceTaskServiceImpl service = service(Mockito.mock(SmtIscDownRecordService.class),
				deviceTaskMapper, Mockito.mock(SmtVisitorMapper.class));
		Mockito.when(deviceTaskMapper.getStaleOfflineDownloadTasks(DeviceTaskConstants.CARD, 3))
				.thenReturn(Collections.emptyList());

		java.util.Set<Long> applyIds = service.cancelStaleOfflineDownloadTasksAndCollectApplyIds(DeviceTaskConstants.CARD, 3);

		Assert.assertTrue(applyIds.isEmpty());
		Mockito.verify(deviceTaskMapper, Mockito.never()).cancelStaleOfflineDownloadTask(Mockito.anyLong(),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
				Mockito.any(), Mockito.any());
	}

	private SmtIscDeviceTask securityAuthTask(Long id, Long sourceId, Long batchId, Integer status,
			LocalDateTime updateTime, String iscTaskId) {
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setId(id);
		task.setSourceType("SECURITY_AUTH");
		task.setSourceId(sourceId);
		task.setSourceDetailId(101L);
		task.setIntentKey("SECURITY_AUTH:11:22:device-1");
		task.setBatchId(batchId);
		task.setStatus(status);
		task.setUpdateTime(updateTime);
		task.setIscTaskId(iscTaskId);
		return task;
	}

	private DeviceTaskVO securityAuthTaskVO(Long sourceId, Long sourceDetailId, Long batchId) {
		DeviceTaskVO taskVO = new DeviceTaskVO();
		taskVO.setAction(DeviceTaskActionEnum.DOWN.getCode());
		taskVO.setDeviceType(DeviceTaskConstants.CARD);
		taskVO.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		taskVO.setDeviceCode("device-1");
		taskVO.setCardNo("11");
		taskVO.setStartTime(1784257200L);
		taskVO.setOverTime(DeviceTaskConstants.maxTime);
		taskVO.setSourceType("SECURITY_AUTH");
		taskVO.setSourceId(sourceId);
		taskVO.setSourceDetailId(sourceDetailId);
		taskVO.setBatchId(batchId);
		taskVO.setIntentKey("SECURITY_AUTH:11:22:device-1");
		return taskVO;
	}

	private void assertQueryHasParam(LambdaQueryWrapper queryWrapper, Object expected) {
		queryWrapper.getSqlSegment();
		Assert.assertTrue(queryWrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(expected).equals(String.valueOf(value))));
	}

	private SmtIscDeviceTaskServiceImpl service(SmtIscDownRecordService downRecordService,
												SmtIscDeviceTaskMapper deviceTaskMapper,
												SmtVisitorMapper visitorMapper) throws Exception {
		return service(downRecordService, deviceTaskMapper, visitorMapper, Mockito.mock(SmtDeviceMapper.class));
	}

	private SmtIscDeviceTaskServiceImpl service(SmtIscDownRecordService downRecordService,
												SmtIscDeviceTaskMapper deviceTaskMapper,
												SmtVisitorMapper visitorMapper,
												SmtDeviceMapper deviceMapper) throws Exception {
		SmtIscDeviceTaskServiceImpl service = new SmtIscDeviceTaskServiceImpl();
		setField(service, "baseMapper", deviceTaskMapper);
		setField(service, "smtIscDownRecordService", downRecordService);
		setField(service, "smtDeviceMapper", deviceMapper);
		setField(service, "smtVisitorMapper", visitorMapper);
		setField(service, "smtFellowVisitorMapper", Mockito.mock(SmtFellowVisitorMapper.class));
		setField(service, "smtAdmittanceFellowMapper", Mockito.mock(SmtAdmittanceFellowMapper.class));
		return service;
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

	private void setOptionalField(Object target, String name, Object value) throws Exception {
		try {
			setField(target, name, value);
		} catch (NoSuchFieldException ignored) {
			// The legacy field may be removed; the behavior must stay disabled either way.
		}
	}
}
