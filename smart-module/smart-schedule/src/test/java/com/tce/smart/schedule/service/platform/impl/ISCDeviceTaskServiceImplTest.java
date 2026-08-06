package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.resp.InternalScheduleIscPersonRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalScheduleStaffIdentityRespDTO;
import com.tce.smart.platform.api.feign.RemoteParkInternalService;
import com.tce.smart.platform.api.feign.RemoteSnapPersonService;
import com.tce.smart.platform.api.feign.RemoteStaffService;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.enums.ISCDeviceTaskEnum;
import com.tce.smart.platform.core.mapper.SmtAdmittanceApplyMapper;
import com.tce.smart.platform.core.mapper.SmtAdmittanceFellowMapper;
import com.tce.smart.platform.core.mapper.SmtFellowVisitorMapper;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.platform.core.service.SmtMsgTempService;
import com.tce.smart.platform.core.service.SmtStaffOtherService;
import com.tce.smart.platform.core.service.SmtVisitorService;
import com.tce.smart.platform.core.entity.SmtImage;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.ISCDeviceTaskErrorEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ISCDeviceTaskServiceImplTest {

	private static final ZoneId ISC_ZONE = ZoneId.of("Asia/Shanghai");
	private static final DateTimeFormatter ISC_TIME_FORMAT =
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	private static final long TEMP_ACCESS_START = LocalDate.now(ISC_ZONE).plusDays(30)
			.atTime(8, 30).atZone(ISC_ZONE).toEpochSecond();
	private static final long TEMP_ACCESS_END = LocalDate.now(ISC_ZONE).plusDays(30)
			.atTime(18, 0).atZone(ISC_ZONE).toEpochSecond();
	private static final String TEMP_ACCESS_START_ISO = formatIscTime(TEMP_ACCESS_START);
	private static final String TEMP_ACCESS_END_ISO = formatIscTime(TEMP_ACCESS_END);

	@Test
	public void trimIscPersonTextParametersRemovesLeadingAndTrailingWhitespaceFromAllTextParameters() {
		Map<String, Object> params = new HashMap<>();
		params.put("personId", " HC0460 ");
		params.put("personName", " 李思翔 ");
		params.put("phoneNo", " 13700893346 ");
		params.put("certificateNo", " 411082200603033070 ");
		params.put("jobNo", " HC0460 ");
		params.put("email", " test@example.com ");
		params.put("gender", 1);

		ISCDeviceTaskServiceImpl.trimIscPersonTextParameters(params);

		Assert.assertEquals("HC0460", params.get("personId"));
		Assert.assertEquals("李思翔", params.get("personName"));
		Assert.assertEquals("13700893346", params.get("phoneNo"));
		Assert.assertEquals("411082200603033070", params.get("certificateNo"));
		Assert.assertEquals("HC0460", params.get("jobNo"));
		Assert.assertEquals("test@example.com", params.get("email"));
		Assert.assertEquals(1, params.get("gender"));
	}

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDeviceTask.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDownRecord.class);
	}

	private static String formatIscTime(long epochSecond) {
		return ISC_TIME_FORMAT.format(Instant.ofEpochSecond(epochSecond).atZone(ISC_ZONE));
	}

	@Test
	public void authConfigDownResultTreatsNoAvailableDeleteDataAsSuccessWhenAuthItemMissing() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(
						ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode())))
				.thenReturn(Result.success("{\"total\":0,\"list\":[]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), task.getCode());
		Assert.assertEquals("删除权限已无可用下发数据，已按删除成功处理", task.getRemark());
		Assert.assertFalse(task.getRemark().contains("isc错误码"));
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(2)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		DispatcherDTO authItemRequest = captor.getAllValues().get(1);
		Assert.assertEquals(EventEnum.ISC_AUTH_ITEM_LIST_GET.getCode(), authItemRequest.getEventType());
		Assert.assertEquals(Integer.valueOf(5000021), authItemRequest.getParkId());
		Map authItemParams = (Map) authItemRequest.getData();
		Assert.assertEquals(Collections.singletonList("person-1"), authItemParams.get("personIds"));
		Assert.assertEquals("acsDevice", authItemParams.get("queryType"));
		Assert.assertEquals(1, authItemParams.get("pageNo"));
		Assert.assertEquals(1, authItemParams.get("pageSize"));
		List<Map> resourceInfos = (List<Map>) authItemParams.get("resourceInfos");
		Assert.assertEquals(1, resourceInfos.size());
		Assert.assertEquals("device-1", resourceInfos.get(0).get("resourceIndexCode"));
		Assert.assertEquals("acsDevice", resourceInfos.get(0).get("resourceType"));
		Assert.assertEquals(Collections.singletonList("1"), resourceInfos.get(0).get("channelNos"));
	}

	@Test
	public void authConfigDownResultTreatsBlankErrorDeleteDataAsSuccessWhenAuthItemMissing() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);
		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(null)))
				.thenReturn(Result.success("{\"total\":0,\"list\":[]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), task.getCode());
		Assert.assertEquals("删除权限已无可用下发数据，已按删除成功处理", task.getRemark());
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(2)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Assert.assertEquals(EventEnum.ISC_AUTH_ITEM_LIST_GET.getCode(), captor.getAllValues().get(1).getEventType());
	}

	@Test
	public void authConfigDownResultTreatsNullAuthItemListAsMissingPermission() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);
		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(null)))
				.thenReturn(Result.success("{\"total\":0,\"list\":null}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), task.getCode());
		Assert.assertEquals("删除权限已无可用下发数据，已按删除成功处理", task.getRemark());
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
	}

	@Test
	public void authConfigDownResultKeepsDeleteRetryWhenAuthItemTotalZeroButListStillExists() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);
		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(null)))
				.thenReturn(Result.success("{\"total\":0,\"list\":[{}]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("下载权限失败_ISC未返回错误码"));
		Assert.assertTrue(task.getRemark().contains("删除任务保留并将在1小时后重试"));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void authConfigDownResultKeepsDeleteRetryWhenAuthItemTotalMissingAndListNull() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);
		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(null)))
				.thenReturn(Result.success("{\"list\":null}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("下载权限失败_ISC未返回错误码"));
		Assert.assertTrue(task.getRemark().contains("删除任务保留并将在1小时后重试"));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void authConfigDownResultKeepsDeleteRetryWhenAuthItemListMissing() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);
		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(null)))
				.thenReturn(Result.success("{\"total\":0}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("下载权限失败_ISC未返回错误码"));
		Assert.assertTrue(task.getRemark().contains("删除任务保留并将在1小时后重试"));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void authConfigDownResultKeepsDeleteRetryWhenAuthItemTotalMissing() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);
		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(null)))
				.thenReturn(Result.success("{\"list\":[]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("下载权限失败_ISC未返回错误码"));
		Assert.assertTrue(task.getRemark().contains("删除任务保留并将在1小时后重试"));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void authConfigDownResultKeepsDeleteRetryWhenAuthItemTotalIsNegativeAndListNull() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);
		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(null)))
				.thenReturn(Result.success("{\"total\":-1,\"list\":null}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("下载权限失败_ISC未返回错误码"));
		Assert.assertTrue(task.getRemark().contains("删除任务保留并将在1小时后重试"));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void authConfigDownResultKeepsDeleteRetryWhenAuthItemTotalIsNegativeAndListEmpty() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);
		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(null)))
				.thenReturn(Result.success("{\"total\":-1,\"list\":[]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("下载权限失败_ISC未返回错误码"));
		Assert.assertTrue(task.getRemark().contains("删除任务保留并将在1小时后重试"));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void authConfigDownResultReResolvesDeletePersonIdBeforeAuthItemCheck() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, Mockito.mock(SmtImageService.class),
				deviceService, downRecordService, remoteStaffService, Mockito.mock(SmtStaffOtherService.class));
		SmtIscDeviceTask task = deleteTask();
		task.setCardNo("2059164347547275265");
		task.setBadge("JA26079");
		task.setPersonId("JA26079");
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		stubScheduleStaff(remoteStaffService, "JA26079", "411082199108142426");
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(
						ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode())))
				.thenReturn(Result.success(singleStaffPersonList("isc-person-current", "JA26079", "face-current")))
				.thenReturn(Result.success("{\"total\":0,\"list\":[]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), task.getCode());
		Assert.assertEquals("isc-person-current", task.getPersonId());
		Mockito.verify(remoteStaffService, Mockito.atLeastOnce()).getScheduleIdentityStaff("2059164347547275265",
				SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(3)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		DispatcherDTO authItemRequest = captor.getAllValues().get(2);
		Assert.assertEquals(EventEnum.ISC_AUTH_ITEM_LIST_GET.getCode(), authItemRequest.getEventType());
		Map authItemParams = (Map) authItemRequest.getData();
		Assert.assertEquals(Collections.singletonList("isc-person-current"), authItemParams.get("personIds"));
	}

	@Test
	public void authConfigDownResultKeepsOriginalDownRecordPersonIdBeforeAuthItemCheck() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, Mockito.mock(SmtImageService.class),
				deviceService, downRecordService, remoteStaffService, Mockito.mock(SmtStaffOtherService.class));
		SmtIscDeviceTask task = deleteTask();
		task.setCardNo("2059164347547275265");
		task.setBadge("JA26079");
		task.setPersonId("JA26079");
		SmtIscDownRecord downRecord = new SmtIscDownRecord();
		downRecord.setCardNo("2059164347547275265");
		downRecord.setBadge("JA26079");
		downRecord.setPersonId("isc-person-old");
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(downRecordService.getOne(Mockito.any())).thenReturn(downRecord);
		stubScheduleStaff(remoteStaffService, "JA26079", "411082199108142426");
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenAnswer(invocation -> {
					DispatcherDTO request = invocation.getArgument(0);
					if (EventEnum.ISC_TASK_PROCESS_GET.getCode().equals(request.getEventType())) {
						return Result.success(downloadProgress(
								ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode()));
					}
					if (EventEnum.ISC_PERSON_GET.getCode().equals(request.getEventType())) {
						return Result.success(singleStaffPersonList("isc-person-current", "JA26079", "face-current"));
					}
					if (EventEnum.ISC_AUTH_ITEM_LIST_GET.getCode().equals(request.getEventType())) {
						return Result.success("{\"total\":0,\"list\":[]}");
					}
					return Result.success("{}");
				});

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals("isc-person-old", task.getPersonId());
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(2)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		DispatcherDTO authItemRequest = captor.getAllValues().get(1);
		Assert.assertEquals(EventEnum.ISC_AUTH_ITEM_LIST_GET.getCode(), authItemRequest.getEventType());
		Map authItemParams = (Map) authItemRequest.getData();
		Assert.assertEquals(Collections.singletonList("isc-person-old"), authItemParams.get("personIds"));
	}

	@Test
	public void authConfigDownResultKeepsDeleteRetryWhenAuthItemStillExistsAndHidesRawCode() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(
						ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode())))
				.thenReturn(Result.success("{\"total\":1,\"list\":[{}]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("下载权限失败_下载错误：无可用数据下载"));
		Assert.assertTrue(task.getRemark().contains("删除任务保留并将在1小时后重试"));
		Assert.assertFalse(task.getRemark().contains("isc错误码"));
		Assert.assertFalse(task.getRemark().contains(
				ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode()));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
		Mockito.verify(dispatcherService, Mockito.times(2)).dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
	}

	@Test
	public void authConfigDownResultHidesUnknownRawIscCodeInDeleteRetryRemark() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = deleteTask();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device());
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress("0x15409999")));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertTrue(task.getRemark().contains("下载权限失败_ISC返回未知错误"));
		Assert.assertFalse(task.getRemark().contains("isc错误码"));
		Assert.assertFalse(task.getRemark().contains("0x15409999"));
		Mockito.verify(dispatcherService, Mockito.times(1)).dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void authConfigDownResultShowsMissingDetailBeforeUnknownIscError() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);
		SmtIscDeviceTask task = downTask();
		Mockito.when(taskService.list(Mockito.any()))
				.thenReturn(Collections.singletonList(task))
				.thenReturn(Collections.emptyList());
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device());
		Mockito.when(deviceService.getById("device-1")).thenReturn(device());
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(partialDownloadProgress("0x15409999")))
				.thenReturn(Result.success(downloadDetailForOtherPerson()))
				.thenReturn(Result.success("{\"total\":0,\"list\":[]}"));

		service.authConfigDownResultHandle();

		ArgumentCaptor<LambdaUpdateWrapper> updateCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(taskService).update(updateCaptor.capture());
		Assert.assertTrue(wrapperHasParam(updateCaptor.getValue(),
				"下载权限失败_下载完成但ISC未返回该人员下载明细（ISC返回未知错误），任务保留并将在下次调度重试"));
		Assert.assertFalse(wrapperHasParam(updateCaptor.getValue(),
				"下载权限失败_ISC返回未知错误，任务保留并将在下次调度重试"));
		Assert.assertFalse(wrapperHasParam(updateCaptor.getValue(), "0x15409999"));
	}

	@Test
	public void authConfigDownResultTreatsNoAvailableDownloadAsSuccessWhenAuthItemExists() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);
		SmtIscDeviceTask task = downTask();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device());
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN)))
				.thenReturn(Result.success(downloadProgress(
						ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode())))
				.thenReturn(Result.success("{\"total\":1,\"list\":[{}]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), task.getCode());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getDesc(), task.getRemark());
		Assert.assertFalse(task.getRemark().contains("ISC已存在权限"));
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
	}

	@Test
	public void authConfigDownResultTreatsMissingDownloadDetailAsSuccessWhenAuthItemExists() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);
		SmtIscDeviceTask task = downTask();
		Mockito.when(taskService.list(Mockito.any()))
				.thenReturn(Collections.singletonList(task))
				.thenReturn(Collections.emptyList());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device());
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN)))
				.thenReturn(Result.success(partialDownloadProgress("0x15409999")))
				.thenReturn(Result.success(downloadDetailForOtherPerson()))
				.thenReturn(Result.success("{\"total\":1,\"list\":[{}]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), task.getCode());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getDesc(), task.getRemark());
		Assert.assertFalse(task.getRemark().contains("ISC已存在权限"));
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
	}

	@Test
	public void authConfigDownResultKeepsDeleteRetryWhenAuthItemQueryThrows() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = deleteTask();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device());
		Mockito.when(deviceService.getById("device-1")).thenReturn(device());
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(
						ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode())))
				.thenThrow(new RuntimeException("auth item query failed"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("下载权限失败_下载错误：无可用数据下载"));
		Assert.assertFalse(task.getRemark().contains("isc错误码"));
		Assert.assertFalse(task.getRemark().contains(
				ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode()));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void authConfigDownResultKeepsDeleteRetryWhenAuthItemQueryReturnsNull() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = deleteTask();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device());
		Mockito.when(deviceService.getById("device-1")).thenReturn(device());
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(
						ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode())))
				.thenReturn(null);

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("下载权限失败_下载错误：无可用数据下载"));
		Assert.assertFalse(task.getRemark().contains("isc错误码"));
		Assert.assertFalse(task.getRemark().contains(
				ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode()));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void authConfigDownResultChecksDeleteTaskInsideMixedBatch() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask deleteTask = deleteTask();
		SmtIscDeviceTask downTask = downTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Arrays.asList(deleteTask, downTask));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(
						ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode())))
				.thenReturn(Result.success("{\"total\":0,\"list\":[]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), deleteTask.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), deleteTask.getCode());
		Assert.assertEquals("删除权限已无可用下发数据，已按删除成功处理", deleteTask.getRemark());
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), downTask.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), downTask.getCode());
		Assert.assertTrue(downTask.getRemark().contains("下载权限失败_下载错误：无可用数据下载"));
		Assert.assertTrue(downTask.getRemark().contains("任务保留并将在下次调度重试"));
		Assert.assertFalse(downTask.getRemark().contains("isc错误码"));
		Assert.assertFalse(downTask.getRemark().contains(
				ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode()));
		Assert.assertNull(downTask.getIscTaskId());
		Mockito.verify(downRecordService).handleTaskDownRecord(deleteTask);
	}

	@Test
	public void authConfigDownResultChecksUnhandledDeleteTaskInPartialFailure() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any()))
				.thenReturn(Collections.singletonList(task))
				.thenReturn(Collections.emptyList());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(partialDownloadProgress(
						ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode())))
				.thenReturn(Result.success(downloadDetailForOtherPerson()))
				.thenReturn(Result.success("{\"total\":0,\"list\":[]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), task.getCode());
		Assert.assertEquals("删除权限已无可用下发数据，已按删除成功处理", task.getRemark());
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
		Mockito.verify(dispatcherService, Mockito.times(3)).dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
	}

	@Test
	public void authConfigDownResultTreatsBlankErrorUnhandledDeleteDataAsSuccessWhenAuthItemMissing() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any()))
				.thenReturn(Collections.singletonList(task))
				.thenReturn(Collections.emptyList());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(partialDownloadProgress(null)))
				.thenReturn(Result.success(downloadDetailForOtherPerson()))
				.thenReturn(Result.success("{\"total\":0,\"list\":[]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), task.getCode());
		Assert.assertEquals("删除权限已无可用下发数据，已按删除成功处理", task.getRemark());
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(3)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Assert.assertEquals(EventEnum.ISC_AUTH_ITEM_LIST_GET.getCode(), captor.getAllValues().get(2).getEventType());
	}

	@Test
	public void authConfigDownResultChecksDeleteTaskWhenPartialDetailIsEmpty() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(partialDownloadProgress(
						ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode())))
				.thenReturn(Result.success("{\"total\":0,\"list\":[]}"))
				.thenReturn(Result.success("{\"total\":0,\"list\":[]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), task.getCode());
		Assert.assertEquals("删除权限已无可用下发数据，已按删除成功处理", task.getRemark());
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
		Mockito.verify(dispatcherService, Mockito.times(3)).dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
	}

	@Test
	public void authConfigDownResultTreatsBlankErrorDeleteDataAsSuccessWhenPartialDetailIsEmpty() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(partialDownloadProgress(null)))
				.thenReturn(Result.success("{\"total\":0,\"list\":[]}"))
				.thenReturn(Result.success("{\"total\":0,\"list\":[]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), task.getCode());
		Assert.assertEquals("删除权限已无可用下发数据，已按删除成功处理", task.getRemark());
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(3)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Assert.assertEquals(EventEnum.ISC_AUTH_ITEM_LIST_GET.getCode(), captor.getAllValues().get(2).getEventType());
	}

	@Test
	public void authConfigDownResultTreatsBlankDetailErrorDeleteDataAsSuccessWhenAuthItemMissing() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any()))
				.thenReturn(Collections.singletonList(task))
				.thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(partialDownloadProgress("0x15409999")))
				.thenReturn(Result.success(downloadDetailForPerson("person-1", null)))
				.thenReturn(Result.success("{\"total\":0,\"list\":[]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), task.getCode());
		Assert.assertEquals("删除权限已无可用下发数据，已按删除成功处理", task.getRemark());
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(3)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Assert.assertEquals(EventEnum.ISC_AUTH_ITEM_LIST_GET.getCode(), captor.getAllValues().get(2).getEventType());
	}

	@Test
	public void authConfigDownResultDoesNotOverrideDetailedDeleteFailureInPartialFailure() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = deleteTask();
		SmtDevice device = device();
		Mockito.when(taskService.list(Mockito.any()))
				.thenReturn(Collections.singletonList(task))
				.thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(partialDownloadProgress(
						ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode())))
				.thenReturn(Result.success(downloadDetailForPerson("person-1",
						ISCDeviceTaskErrorEnum.DOWNLOAD_PERMISSION_PACKET_SEND_FAILED.getErrorCode())))
				.thenReturn(Result.success("{\"total\":0,\"list\":[]}"));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("下载权限失败_下载错误：权限报文下发失败"));
		Assert.assertTrue(task.getRemark().contains("删除任务保留并将在1小时后重试"));
		Assert.assertFalse(task.getRemark().contains("isc错误码"));
		Assert.assertFalse(task.getRemark().contains(
				ISCDeviceTaskErrorEnum.DOWNLOAD_PERMISSION_PACKET_SEND_FAILED.getErrorCode()));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void authConfigDownResultUsesOfficialCodeDescriptionWhenEnumWasPreviouslyMissing() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = downTask();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device());
		Mockito.when(deviceService.getById("device-1")).thenReturn(device());
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(
						ISCDeviceTaskErrorEnum.DOWNLOAD_CARD_PERMISSION_UNSUPPORTED.getErrorCode())));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("下载权限失败_下载错误：不支持卡片权限"));
		Assert.assertTrue(task.getRemark().contains("任务保留并将在下次调度重试"));
		Assert.assertFalse(task.getRemark().contains(
				ISCDeviceTaskErrorEnum.DOWNLOAD_CARD_PERMISSION_UNSUPPORTED.getErrorCode()));
		Assert.assertNull(task.getIscTaskId());
	}

	@Test
	public void authConfigDownResultRetriesNormalDownloadTaskWhenWholeResourceFails() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = downTask();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device());
		Mockito.when(deviceService.getById("device-1")).thenReturn(device());
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(null)));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), task.getCode());
		Assert.assertEquals("下载权限失败_ISC未返回错误码，任务保留并将在下次调度重试", task.getRemark());
		Assert.assertNull(task.getIscTaskId());
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void authConfigDownResultStopsNormalDownloadTaskAfterMaxRetryTimes() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = downTask();
		task.setTimes(10);
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device());
		Mockito.when(deviceService.getById("device-1")).thenReturn(device());
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(downloadProgress(null)));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.FAIL.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("已达到最大重试次数10次"));
		Assert.assertTrue(task.getRemark().contains("请人工介入"));
		Assert.assertNull(task.getIscTaskId());
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void authConfigDownResultIncludesNestedDetailErrorsWithoutRawCodes() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, deviceService, downRecordService);

		SmtIscDeviceTask task = downTask();
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(task));
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		Mockito.when(deviceService.getOne(Mockito.any())).thenReturn(device());
		Mockito.when(deviceService.getById("device-1")).thenReturn(device());
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(partialDownloadProgress(
						ISCDeviceTaskErrorEnum.DOWNLOAD_PERMISSION_PACKET_SEND_FAILED.getErrorCode())))
				.thenReturn(Result.success(downloadDetailWithNestedErrors("person-2")));

		service.authConfigDownResultHandle();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("下载权限失败_下载错误：权限报文下发失败"));
		Assert.assertTrue(task.getRemark().contains("回调错误：卡号错误"));
		Assert.assertTrue(task.getRemark().contains("下载错误：人员没有指纹"));
		Assert.assertTrue(task.getRemark().contains("任务保留并将在下次调度重试"));
		Assert.assertFalse(task.getRemark().contains(
				ISCDeviceTaskErrorEnum.CALLBACK_CARD_NUMBER_INVALID.getErrorCode()));
		Assert.assertFalse(task.getRemark().contains(
				ISCDeviceTaskErrorEnum.DOWNLOAD_PERSON_NO_FINGERPRINT.getErrorCode()));
		Assert.assertFalse(task.getRemark().contains("isc错误码"));
		Assert.assertNull(task.getIscTaskId());
	}

	@Test
	public void downAccessStopsExceededRetriesBeforeCancelingStaleOfflineTasks() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService);
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt())).thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.emptyList());
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());

		service.downAccess();

		// 停止超限重试与取消长期离线均已切换到AndCollectApplyIds重载（批量终态补聚合钩子），执行顺序不变
		org.mockito.InOrder inOrder = Mockito.inOrder(taskService);
		inOrder.verify(taskService).stopExceededRetryAuthTasksAndCollectApplyIds(
				Mockito.eq(DeviceTaskConstants.CARD),
				Mockito.eq(DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES),
				Mockito.contains("请人工介入"));
		inOrder.verify(taskService).cancelStaleOfflineDownloadTasksAndCollectApplyIds(
				Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt());
	}

	@Test
	public void downAccessTriggersAggregationPerApplyIdForEachBulkTerminalUpdate() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtAdmittanceApplyMapper admittanceApplyMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService,
				Mockito.mock(SmtImageService.class), admittanceApplyMapper);
		// 四个批量终态方法各返回一个受影响申请单：验证每个批量出口都逐单触发聚合
		Mockito.when(taskService.stopExceededRetryAuthTasksAndCollectApplyIds(
				Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt(), Mockito.anyString()))
				.thenReturn(Collections.singleton(101L));
		Mockito.when(taskService.expireOverdueDownloadTasksAndCollectApplyIds(DeviceTaskConstants.CARD))
				.thenReturn(Collections.singleton(102L));
		Mockito.when(taskService.markOfflineDeviceTasksAndCollectApplyIds(DeviceTaskConstants.CARD))
				.thenReturn(Collections.singleton(103L));
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(
				Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt()))
				.thenReturn(Collections.singleton(104L));
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.emptyList());
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		// 聚合器的第一步是selectById查申请单：返回null让aggregate提前返回，仅验证"被触发"这一行为
		Mockito.when(admittanceApplyMapper.selectById(Mockito.anyLong())).thenReturn(null);

		service.downAccess();

		Mockito.verify(admittanceApplyMapper).selectById(101L);
		Mockito.verify(admittanceApplyMapper).selectById(102L);
		Mockito.verify(admittanceApplyMapper).selectById(103L);
		Mockito.verify(admittanceApplyMapper).selectById(104L);
	}

	@Test
	public void downAccessSkipsAggregationWhenBulkTerminalUpdatesAffectNoApply() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtAdmittanceApplyMapper admittanceApplyMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService,
				Mockito.mock(SmtImageService.class), admittanceApplyMapper);
		// 无匹配行：四个批量终态方法均返回空集，不得触发任何聚合查询
		Mockito.when(taskService.stopExceededRetryAuthTasksAndCollectApplyIds(
				Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt(), Mockito.anyString()))
				.thenReturn(Collections.emptySet());
		Mockito.when(taskService.expireOverdueDownloadTasksAndCollectApplyIds(DeviceTaskConstants.CARD))
				.thenReturn(Collections.emptySet());
		Mockito.when(taskService.markOfflineDeviceTasksAndCollectApplyIds(DeviceTaskConstants.CARD))
				.thenReturn(Collections.emptySet());
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(
				Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt()))
				.thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.emptyList());
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());

		service.downAccess();

		Mockito.verifyZeroInteractions(admittanceApplyMapper);
	}

	@Test
	public void downAccessContinuesAggregationWhenSingleApplyAggregationFails() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtAdmittanceApplyMapper admittanceApplyMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService,
				Mockito.mock(SmtImageService.class), admittanceApplyMapper);
		// LinkedHashSet保证迭代顺序：201先聚合（抛异常），202在其后仍必须被聚合（per-apply try/catch隔离）
		java.util.Set<Long> applyIds = new java.util.LinkedHashSet<>(Arrays.asList(201L, 202L));
		Mockito.when(taskService.stopExceededRetryAuthTasksAndCollectApplyIds(
				Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt(), Mockito.anyString()))
				.thenReturn(applyIds);
		Mockito.when(taskService.expireOverdueDownloadTasksAndCollectApplyIds(DeviceTaskConstants.CARD))
				.thenReturn(Collections.emptySet());
		Mockito.when(taskService.markOfflineDeviceTasksAndCollectApplyIds(DeviceTaskConstants.CARD))
				.thenReturn(Collections.emptySet());
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(
				Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt()))
				.thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.emptyList());
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(admittanceApplyMapper.selectById(201L)).thenThrow(new RuntimeException("模拟聚合查询异常"));
		Mockito.when(admittanceApplyMapper.selectById(202L)).thenReturn(null);

		service.downAccess();

		// 201聚合异常被捕获记ERROR后，202仍被逐单触发
		Mockito.verify(admittanceApplyMapper).selectById(201L);
		Mockito.verify(admittanceApplyMapper).selectById(202L);
	}

	@Test
	public void downAccessKeepsDistinctVisitorPersonIdsWhenLocalCardNoIsMissing() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService);
		SmtIscDeviceTask firstVisitor = visitorTaskWithoutLocalCard(1L, "cert-1");
		SmtIscDeviceTask secondVisitor = visitorTaskWithoutLocalCard(2L, "cert-2");
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt())).thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Arrays.asList(firstVisitor, secondVisitor));
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(imageService.getImageBinaryByCode(Mockito.anyString())).thenReturn(new byte[20 * 1024]);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(visitorPersonList()))
				.thenReturn(Result.success("{\"taskId\":\"config-task-1\"}"));

		service.downAccess();

		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(2)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Map params = (Map) captor.getAllValues().get(1).getData();
		Object[] personDatas = (Object[]) params.get("personDatas");
		Map personInfo = (Map) personDatas[0];
		Assert.assertEquals(Arrays.asList("person-1", "person-2"), personInfo.get("indexCodes"));
	}

	@Test
	public void downAccessQueriesRetryablePersonCreationTasks() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService,
				Mockito.mock(SmtImageService.class));
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt()))
				.thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.emptyList());
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.getReTryCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());

		service.downAccess();

		Mockito.verify(taskService).getReTryCardDown(Mockito.any(Page.class), Mockito.anyLong(),
				Mockito.eq(DeviceTaskConstants.CARD));
	}

	@Test
	public void downAccessRetainsPersonCreationFailureAsRetryable502WithOriginalIscMessage() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService);
		SmtIscDeviceTask visitor = visitorTask(30L, "9990000030", "cert-create-failure");
		visitor.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
		visitor.setGeneral("建人失败测试访客");
		SmtImage image = new SmtImage();
		image.setImage(new byte[20 * 1024]);
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt()))
				.thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.singletonList(visitor));
		Mockito.when(taskService.getReTryCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(imageService.getByCode(Mockito.anyString())).thenReturn(image);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN)))
				.thenAnswer(invocation -> {
					DispatcherDTO request = invocation.getArgument(0);
					if (EventEnum.ISC_PERSON_GET.getCode().equals(request.getEventType())) {
						return Result.success(emptyPersonList());
					}
					if (EventEnum.ISC_PERSON_ADD.getCode().equals(request.getEventType())) {
						return Result.fail("ISC原始错误：证件号格式错误");
					}
					return Result.fail("不应继续下发权限");
				});

		service.downAccess();

		Assert.assertEquals(ISCDeviceTaskEnum.ADD_PERSON_ERROR.getCode(), visitor.getCode());
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), visitor.getStatus());
		Assert.assertEquals(Integer.valueOf(1), visitor.getTimes());
		Assert.assertTrue(visitor.getRemark().contains("ISC原始错误：证件号格式错误"));
		Assert.assertFalse(visitor.getRemark().contains(ISCDeviceTaskEnum.PERSON_NOT_EXIST.getDesc()));
	}

	@Test
	public void downAccessCreatesOnePersonForSameParkAndPersonAcrossTwoDevices() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService);
		SmtIscDeviceTask firstVisitor = visitorTask(31L, "9990000031", "cert-shared");
		firstVisitor.setDeviceCode("device-shared-1");
		firstVisitor.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
		firstVisitor.setGeneral("共享建人测试访客");
		SmtIscDeviceTask secondVisitor = visitorTask(32L, "9990000032", "cert-shared");
		secondVisitor.setDeviceCode("device-shared-2");
		secondVisitor.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
		secondVisitor.setGeneral("共享建人测试访客");
		SmtImage image = new SmtImage();
		image.setImage(new byte[20 * 1024]);
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt()))
				.thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Arrays.asList(firstVisitor, secondVisitor));
		Mockito.when(taskService.getReTryCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(imageService.getByCode(Mockito.anyString())).thenReturn(image);
		Mockito.when(imageService.getImageBinaryByCode(Mockito.anyString())).thenReturn(new byte[20 * 1024]);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN)))
				.thenAnswer(invocation -> {
					DispatcherDTO request = invocation.getArgument(0);
					if (EventEnum.ISC_PERSON_GET.getCode().equals(request.getEventType())) {
						return Result.success(emptyPersonList());
					}
					if (EventEnum.ISC_PERSON_ADD.getCode().equals(request.getEventType())) {
						return Result.success("{\"personId\":\"person-shared\"}");
					}
					if (EventEnum.ISC_AUTH_CONFIG_ADD.getCode().equals(request.getEventType())) {
						return Result.success("{\"taskId\":\"auth-task\"}");
					}
					return Result.fail("未预期的ISC请求");
				});

		service.downAccess();

		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.atLeastOnce()).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN));
		long personAddCount = captor.getAllValues().stream()
				.filter(request -> EventEnum.ISC_PERSON_ADD.getCode().equals(request.getEventType()))
				.count();
		long authConfigAddCount = captor.getAllValues().stream()
				.filter(request -> EventEnum.ISC_AUTH_CONFIG_ADD.getCode().equals(request.getEventType()))
				.count();
		Assert.assertEquals(1L, personAddCount);
		Assert.assertEquals(2L, authConfigAddCount);
	}

	@Test
	public void downAccessSkipsVisitorTasksWhenVisitorBatchQueryFails() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService);
		SmtIscDeviceTask visitor = visitorTaskWithoutLocalCard(1L, "cert-1");
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt())).thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.singletonList(visitor));
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		// 批量查询临时人员失败：任务应整体跳过，保持INIT等待下轮，不得走"人员不存在→新增人员"
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.fail("ISC平台查询超时"));

		service.downAccess();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), visitor.getStatus());
		Assert.assertNull(visitor.getTimes());
		Mockito.verify(taskService, Mockito.never()).updateById(Mockito.any(SmtIscDeviceTask.class));
		// 只允许发生一次批量人员查询，不允许出现新增人员/权限配置等后续调用
		Mockito.verify(dispatcherService, Mockito.times(1))
				.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
	}

	@Test
	public void downAccessUsesVisitorCertNoWhenCardNoIsVirtual() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService);
		SmtIscDeviceTask visitor = visitorTask(3L, "9990000003", "cert-999");
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt())).thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.singletonList(visitor));
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(imageService.getImageBinaryByCode(Mockito.anyString())).thenReturn(new byte[20 * 1024]);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(singleVisitorPersonList("person-999", "cert-999")))
				.thenReturn(Result.success("{\"taskId\":\"config-task-999\"}"));

		service.downAccess();

		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(2)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Map personQueryParams = (Map) captor.getAllValues().get(0).getData();
		Assert.assertEquals("cert-999", ((Iterable) personQueryParams.get("paramValue")).iterator().next());
		Map authParams = (Map) captor.getAllValues().get(1).getData();
		Object[] personDatas = (Object[]) authParams.get("personDatas");
		Map personInfo = (Map) personDatas[0];
		Assert.assertEquals(Collections.singletonList("person-999"), personInfo.get("indexCodes"));
		Assert.assertEquals(TEMP_ACCESS_START_ISO, authParams.get("startTime"));
		Assert.assertEquals(TEMP_ACCESS_END_ISO, authParams.get("endTime"));
	}

	@Test
	public void downAccessSkipsVehicleTasksEvenIfLegacySwitchEnabled() throws Exception {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService);
		setOptionalField(service, "iscVehicleAuthEnabled", true);
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt())).thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.emptyList());
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());

		service.downAccess();

		Mockito.verify(taskService, Mockito.never()).getCardDown(Mockito.any(Page.class), Mockito.anyLong(),
				Mockito.eq(DeviceTaskConstants.CAR));
		Mockito.verify(taskService, Mockito.never()).getDelayDown(Mockito.any(Page.class), Mockito.anyLong(),
				Mockito.eq(DeviceTaskConstants.CAR));
		Mockito.verify(imageService, Mockito.never()).getImageBinaryByCode(Mockito.anyString());
		Mockito.verify(dispatcherService, Mockito.never()).dispatch(Mockito.any(DispatcherDTO.class),
				Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		ArgumentCaptor<LambdaUpdateWrapper> updateCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(taskService).update(updateCaptor.capture());
		Assert.assertTrue(updateCaptor.getValue().getParamNameValuePairs().values().stream()
				.anyMatch(value -> queryParamMatches(value, DeviceTaskStatusEnum.CANCEL.getCode())));
		String updateSql = updateCaptor.getValue().getSqlSegment().toUpperCase();
		Assert.assertTrue(updateSql.contains("DEVICE_TYPE"));
		Assert.assertFalse(updateSql.contains("SERVICE_TYPE"));
	}

	@Test
	public void downAccessUpdatesExistingStaffFaceInDeviceParkForUpdateFaceTask() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		SmtStaffOtherService staffOtherService = Mockito.mock(SmtStaffOtherService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService,
				Mockito.mock(RemoteStaffService.class), staffOtherService);
		SmtIscDeviceTask staffTask = staffUpdateFaceTask();
		SmtStaff staff = new SmtStaff();
		staff.setId(1001L);
		staff.setBadge("JA26086");
		SmtImage image = new SmtImage();
		image.setImage(new byte[20 * 1024]);
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt())).thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.singletonList(staffTask));
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(staffOtherService.list(Mockito.any())).thenReturn(Collections.singletonList(staff));
		Mockito.when(imageService.getImageBinaryByCode("image-staff")).thenReturn(new byte[20 * 1024]);
		Mockito.when(imageService.getByCode("image-staff")).thenReturn(image);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(singleStaffPersonList("person-staff", "JA26086", "face-old")))
				.thenReturn(Result.success("{\"faceId\":\"face-new\"}"))
				.thenReturn(Result.success("{\"taskId\":\"config-task-staff\"}"));

		service.downAccess();

		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(3)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		DispatcherDTO faceUpdateRequest = captor.getAllValues().get(1);
		Assert.assertEquals(EventEnum.ISC_FACE_UPDATE.getCode(), faceUpdateRequest.getEventType());
		Assert.assertEquals(Integer.valueOf(5000022), faceUpdateRequest.getParkId());
		Map faceParams = (Map) faceUpdateRequest.getData();
		Assert.assertEquals("face-old", faceParams.get("faceId"));
	}

	@Test
	public void downAccessUpdatesExistingStaffFaceSeparatelyPerDevicePark() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		SmtStaffOtherService staffOtherService = Mockito.mock(SmtStaffOtherService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService,
				Mockito.mock(RemoteStaffService.class), staffOtherService);
		SmtIscDeviceTask xcTask = staffUpdateFaceTask(16L, "device-xc", 5000021, "image-xc");
		SmtIscDeviceTask hfTask = staffUpdateFaceTask(17L, "device-hf", 5000022, "image-hf");
		SmtStaff staff = new SmtStaff();
		staff.setId(1001L);
		staff.setBadge("JA26086");
		SmtImage image = new SmtImage();
		image.setImage(new byte[20 * 1024]);
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt())).thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Arrays.asList(xcTask, hfTask));
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(staffOtherService.list(Mockito.any())).thenReturn(Collections.singletonList(staff));
		Mockito.when(imageService.getImageBinaryByCode(Mockito.anyString())).thenReturn(new byte[20 * 1024]);
		Mockito.when(imageService.getByCode(Mockito.anyString())).thenReturn(image);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenAnswer(invocation -> {
					DispatcherDTO request = invocation.getArgument(0);
					if (EventEnum.ISC_PERSON_GET.getCode().equals(request.getEventType())
							&& Integer.valueOf(5000021).equals(request.getParkId())) {
						return Result.success(singleStaffPersonList("person-xc", "JA26086", "face-xc"));
					}
					if (EventEnum.ISC_PERSON_GET.getCode().equals(request.getEventType())
							&& Integer.valueOf(5000022).equals(request.getParkId())) {
						return Result.success(singleStaffPersonList("person-hf", "JA26086", "face-hf"));
					}
					if (EventEnum.ISC_FACE_UPDATE.getCode().equals(request.getEventType())) {
						return Result.success("{\"faceId\":\"face-new\"}");
					}
					if (EventEnum.ISC_AUTH_CONFIG_ADD.getCode().equals(request.getEventType())) {
						return Result.success("{\"taskId\":\"config-task\"}");
					}
					return Result.success("{}");
				});

		service.downAccess();

		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(6)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Map<Integer, String> faceIdByPark = new HashMap<>();
		for (DispatcherDTO request : captor.getAllValues()) {
			if (EventEnum.ISC_FACE_UPDATE.getCode().equals(request.getEventType())) {
				Map faceParams = (Map) request.getData();
				faceIdByPark.put(request.getParkId(), String.valueOf(faceParams.get("faceId")));
			}
		}
		Assert.assertEquals(2, faceIdByPark.size());
		Assert.assertEquals("face-xc", faceIdByPark.get(5000021));
		Assert.assertEquals("face-hf", faceIdByPark.get(5000022));
	}

	@Test
	public void faceProcessMapKeyIncludesDeviceParkForStaffCardImports() throws Exception {
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(Mockito.mock(RemoteDispatcherService.class),
				Mockito.mock(SmtIscDeviceTaskService.class), Mockito.mock(SmtImageService.class),
				Mockito.mock(RemoteStaffService.class), Mockito.mock(SmtStaffOtherService.class));
		SmtIscDeviceTask xcTask = staffCardImportTask(18L, "device-xc", 5000021, "image-xc");
		SmtIscDeviceTask hfTask = staffCardImportTask(19L, "device-hf", 5000022, "image-hf");
		Method method = ISCDeviceTaskServiceImpl.class
				.getDeclaredMethod("faceProcessMapKey", SmtIscDeviceTask.class, String.class);
		method.setAccessible(true);

		String xcKey = String.valueOf(method.invoke(service, xcTask, "person-shared"));
		String hfKey = String.valueOf(method.invoke(service, hfTask, "person-shared"));

		Assert.assertNotEquals(xcKey, hfKey);
		Assert.assertTrue(xcKey.contains("park:5000021"));
		Assert.assertTrue(hfKey.contains("park:5000022"));
	}

	@Test
	public void faceProcessMapKeyIncludesImageForTemporaryAccessTasks() throws Exception {
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(Mockito.mock(RemoteDispatcherService.class),
				Mockito.mock(SmtIscDeviceTaskService.class), Mockito.mock(SmtImageService.class),
				Mockito.mock(RemoteStaffService.class), Mockito.mock(SmtStaffOtherService.class));
		SmtIscDeviceTask firstPhotoTask = visitorTask(20L, "visitor-card-1", "cert-1");
		SmtIscDeviceTask secondPhotoTask = visitorTask(21L, "visitor-card-2", "cert-1");
		firstPhotoTask.setImageId("image-first");
		secondPhotoTask.setImageId("image-second");
		Method method = ISCDeviceTaskServiceImpl.class
				.getDeclaredMethod("faceProcessMapKey", SmtIscDeviceTask.class, String.class);
		method.setAccessible(true);

		String firstPhotoKey = String.valueOf(method.invoke(service, firstPhotoTask, "person-shared"));
		String secondPhotoKey = String.valueOf(method.invoke(service, secondPhotoTask, "person-shared"));

		Assert.assertNotEquals(firstPhotoKey, secondPhotoKey);
		Assert.assertTrue(firstPhotoKey.contains("image:image-first"));
		Assert.assertTrue(secondPhotoKey.contains("image:image-second"));
	}

	@Test
	public void personDetailMapKeyKeepsTemporaryTasksSeparateWhenIdentityIsMissing() throws Exception {
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(Mockito.mock(RemoteDispatcherService.class),
				Mockito.mock(SmtIscDeviceTaskService.class), Mockito.mock(SmtImageService.class));
		SmtIscDeviceTask firstTask = visitorTask(23L, "9990000023", null);
		SmtIscDeviceTask secondTask = visitorTask(24L, "9990000024", null);
		Method method = ISCDeviceTaskServiceImpl.class.getDeclaredMethod("personDetailMapKey", SmtIscDeviceTask.class);
		method.setAccessible(true);

		String firstKey = String.valueOf(method.invoke(service, firstTask));
		String secondKey = String.valueOf(method.invoke(service, secondTask));

		Assert.assertNotEquals(firstKey, secondKey);
	}

	@Test
	public void downAccessAddsMissingStaffFaceSeparatelyPerDevicePark() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		SmtStaffOtherService staffOtherService = Mockito.mock(SmtStaffOtherService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService,
				Mockito.mock(RemoteStaffService.class), staffOtherService);
		SmtIscDeviceTask xcTask = staffCardImportTask(18L, "device-xc", 5000021, "image-xc");
		SmtIscDeviceTask hfTask = staffCardImportTask(19L, "device-hf", 5000022, "image-hf");
		SmtStaff staff = new SmtStaff();
		staff.setId(1001L);
		staff.setBadge("JA26086");
		SmtImage image = new SmtImage();
		image.setImage(new byte[20 * 1024]);
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt())).thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Arrays.asList(xcTask, hfTask));
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(staffOtherService.list(Mockito.any())).thenReturn(Collections.singletonList(staff));
		Mockito.when(imageService.getByCode(Mockito.anyString())).thenReturn(image);
		Mockito.when(imageService.getImageBinaryByCode(Mockito.anyString())).thenReturn(new byte[20 * 1024]);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenAnswer(invocation -> {
					DispatcherDTO request = invocation.getArgument(0);
					if (EventEnum.ISC_PERSON_GET.getCode().equals(request.getEventType())) {
						return Result.success(singleStaffPersonListWithoutFace("person-shared", "JA26086"));
					}
					if (EventEnum.ISC_FACE_ADD.getCode().equals(request.getEventType())) {
						return Result.success("{\"faceId\":\"face-new\"}");
					}
					if (EventEnum.ISC_AUTH_CONFIG_ADD.getCode().equals(request.getEventType())) {
						return Result.success("{\"taskId\":\"config-task\"}");
					}
					return Result.success("{}");
				});

		service.downAccess();

		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(6)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Map<Integer, String> addFacePersonByPark = new HashMap<>();
		for (DispatcherDTO request : captor.getAllValues()) {
			if (EventEnum.ISC_FACE_ADD.getCode().equals(request.getEventType())) {
				Map faceParams = (Map) request.getData();
				addFacePersonByPark.put(request.getParkId(), String.valueOf(faceParams.get("personId")));
			}
		}
		Assert.assertEquals(2, addFacePersonByPark.size());
		Assert.assertEquals("person-shared", addFacePersonByPark.get(5000021));
		Assert.assertEquals("person-shared", addFacePersonByPark.get(5000022));
	}

	@Test
	public void addOrDelAuthConfigCancelsVehicleTasksBeforeDispatch() throws Exception {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService);
		SmtIscDeviceTask carTask = new SmtIscDeviceTask();
		carTask.setId(13L);
		carTask.setAction(DeviceTaskActionEnum.DOWN.getCode());
		carTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		carTask.setDeviceType(DeviceTaskConstants.CAR);
		carTask.setServiceType(DeviceTaskConstants.CAT_ADMITTANCE);
		carTask.setDeviceCode("car-device-1");
		carTask.setCardNo("4001");
		carTask.setStartTime(TEMP_ACCESS_START);
		carTask.setOverTime(TEMP_ACCESS_END);
		carTask.setParkId(5000021);
		Method addOrDelAuthConfig = ISCDeviceTaskServiceImpl.class
				.getDeclaredMethod("addOrDelAuthConfig", List.class, boolean.class);
		addOrDelAuthConfig.setAccessible(true);

		Boolean result = (Boolean) addOrDelAuthConfig.invoke(service, Collections.singletonList(carTask), true);

		Assert.assertTrue(result);
		ArgumentCaptor<SmtIscDeviceTask> taskCaptor = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(taskService).updateById(taskCaptor.capture());
		Assert.assertEquals(DeviceTaskStatusEnum.CANCEL.getCode(), taskCaptor.getValue().getStatus());
		Assert.assertEquals("ISC车辆权限不支持下发，已取消", taskCaptor.getValue().getRemark());
		Mockito.verify(dispatcherService, Mockito.never()).dispatch(Mockito.any(DispatcherDTO.class),
				Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Mockito.verify(imageService, Mockito.never()).getImageBinaryByCode(Mockito.anyString());
	}

	@Test
	public void downAccessCreatesTemporaryPersonByCertNoWhenCardNoIsVirtualAndIscPersonMissing() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtStaffOtherService staffOtherService = Mockito.mock(SmtStaffOtherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService,
				remoteStaffService, staffOtherService);
		SmtIscDeviceTask admittanceTask = visitorTask(4L, "9990000004", "cert-admittance");
		admittanceTask.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
		admittanceTask.setGeneral("admittance visitor");
		SmtImage image = new SmtImage();
		image.setImage(new byte[20 * 1024]);
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt())).thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.singletonList(admittanceTask));
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(imageService.getByCode(Mockito.anyString())).thenReturn(image);
		Mockito.when(imageService.getImageBinaryByCode(Mockito.anyString())).thenReturn(new byte[20 * 1024]);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(emptyPersonList()))
				.thenReturn(Result.success(emptyPersonList()))
				.thenReturn(Result.success("{\"personId\":\"person-generated\"}"))
				.thenReturn(Result.success("{\"taskId\":\"config-task-generated\"}"));

		service.downAccess();

		verifyNoScheduleStaffLookup(remoteStaffService);
		Mockito.verify(staffOtherService, Mockito.never()).getOne(Mockito.any());
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(4)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		DispatcherDTO personAddRequest = captor.getAllValues().get(2);
		Assert.assertEquals(EventEnum.ISC_PERSON_ADD.getCode(), personAddRequest.getEventType());
		Map personAddParams = (Map) personAddRequest.getData();
		Assert.assertEquals("admittance visitor", personAddParams.get("personName"));
		Assert.assertEquals("cert-admittance", personAddParams.get("certificateNo"));
		Assert.assertFalse(personAddParams.containsKey("personId"));
		Assert.assertFalse(personAddParams.containsKey("jobNo"));
		DispatcherDTO authRequest = captor.getAllValues().get(3);
		Assert.assertEquals(EventEnum.ISC_AUTH_CONFIG_ADD.getCode(), authRequest.getEventType());
		Map authParams = (Map) authRequest.getData();
		Object[] personDatas = (Object[]) authParams.get("personDatas");
		Map personInfo = (Map) personDatas[0];
		Assert.assertEquals(Collections.singletonList("person-generated"), personInfo.get("indexCodes"));
		Assert.assertEquals(TEMP_ACCESS_START_ISO, authParams.get("startTime"));
		Assert.assertEquals(TEMP_ACCESS_END_ISO, authParams.get("endTime"));
	}

	@Test
	public void downAccessUpdatesExistingTemporaryPersonFaceBeforeAuthConfig() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtStaffOtherService staffOtherService = Mockito.mock(SmtStaffOtherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService,
				remoteStaffService, staffOtherService);
		SmtIscDeviceTask admittanceTask = visitorTask(14L, "2063507857263071234", "421081198506080639");
		admittanceTask.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
		admittanceTask.setGeneral("熊俊");
		SmtImage image = new SmtImage();
		image.setImage(new byte[20 * 1024]);
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt())).thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.singletonList(admittanceTask));
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(imageService.getImageBinaryByCode("image-14")).thenReturn(new byte[20 * 1024]);
		Mockito.when(imageService.getByCode("image-14")).thenReturn(image);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(singleVisitorPersonList("sz20652", "421081198506080639")))
				.thenReturn(Result.success("{\"faceId\":\"face-updated\"}"))
				.thenReturn(Result.success("{\"taskId\":\"config-task\"}"));

		service.downAccess();

		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(3)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		DispatcherDTO faceUpdateRequest = captor.getAllValues().get(1);
		Assert.assertEquals(EventEnum.ISC_FACE_UPDATE.getCode(), faceUpdateRequest.getEventType());
		Assert.assertEquals(Integer.valueOf(5000021), faceUpdateRequest.getParkId());
		Map faceParams = (Map) faceUpdateRequest.getData();
		Assert.assertEquals("face-sz20652", faceParams.get("faceId"));
		Assert.assertTrue(faceParams.containsKey("faceData"));
		DispatcherDTO authRequest = captor.getAllValues().get(2);
		Assert.assertEquals(EventEnum.ISC_AUTH_CONFIG_ADD.getCode(), authRequest.getEventType());
		Map authParams = (Map) authRequest.getData();
		Object[] personDatas = (Object[]) authParams.get("personDatas");
		Map personInfo = (Map) personDatas[0];
		Assert.assertEquals(Collections.singletonList("sz20652"), personInfo.get("indexCodes"));
	}

	@Test
	public void downAccessUpdatesExistingTemporaryPersonFaceWhenFallbackPersonQueryFindsPerson() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtStaffOtherService staffOtherService = Mockito.mock(SmtStaffOtherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService,
				remoteStaffService, staffOtherService);
		SmtIscDeviceTask admittanceTask = visitorTask(22L, "2063507857263071234", "421081198506080639");
		admittanceTask.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
		admittanceTask.setGeneral("熊俊");
		SmtImage image = new SmtImage();
		image.setImage(new byte[20 * 1024]);
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt())).thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.singletonList(admittanceTask));
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(imageService.getImageBinaryByCode("image-22")).thenReturn(new byte[20 * 1024]);
		Mockito.when(imageService.getByCode("image-22")).thenReturn(image);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(emptyPersonList()))
				.thenReturn(Result.success(singleVisitorPersonList("sz20652", "421081198506080639")))
				.thenReturn(Result.success("{\"faceId\":\"face-updated\"}"))
				.thenReturn(Result.success("{\"taskId\":\"config-task\"}"));

		service.downAccess();

		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(4)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Assert.assertEquals(EventEnum.ISC_PERSON_GET.getCode(), captor.getAllValues().get(0).getEventType());
		Assert.assertEquals(EventEnum.ISC_PERSON_GET.getCode(), captor.getAllValues().get(1).getEventType());
		DispatcherDTO faceUpdateRequest = captor.getAllValues().get(2);
		Assert.assertEquals(EventEnum.ISC_FACE_UPDATE.getCode(), faceUpdateRequest.getEventType());
		Map faceParams = (Map) faceUpdateRequest.getData();
		Assert.assertEquals("face-sz20652", faceParams.get("faceId"));
		DispatcherDTO authRequest = captor.getAllValues().get(3);
		Assert.assertEquals(EventEnum.ISC_AUTH_CONFIG_ADD.getCode(), authRequest.getEventType());
		Map authParams = (Map) authRequest.getData();
		Object[] personDatas = (Object[]) authParams.get("personDatas");
		Map personInfo = (Map) personDatas[0];
		Assert.assertEquals(Collections.singletonList("sz20652"), personInfo.get("indexCodes"));
	}

	@Test
	public void downAccessResolvesStaffPersonIdAfterAddResponseOmitsPersonId() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtStaffOtherService staffOtherService = Mockito.mock(SmtStaffOtherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService,
				remoteStaffService, staffOtherService);
		SmtIscDeviceTask staffTask = staffCardImportTask(20L, "device-1", 5000021, "image-current");
		staffTask.setCardNo("2059164347547275265");
		staffTask.setBadge("JA26079");
		SmtStaff staff = staff("2059164347547275265", "JA26079", StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode());
		SmtImage image = new SmtImage();
		image.setImage(new byte[20 * 1024]);
		final int[] personGetCalls = {0};
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt())).thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.singletonList(staffTask));
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(staffOtherService.list(Mockito.any())).thenReturn(Collections.singletonList(staff));
		Mockito.when(imageService.getImageBinaryByCode("image-current")).thenReturn(new byte[20 * 1024]);
		Mockito.when(imageService.getByCode("image-current")).thenReturn(image);
		stubScheduleStaff(remoteStaffService, "JA26079", "411082199108142426");
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenAnswer(invocation -> {
					DispatcherDTO request = invocation.getArgument(0);
					if (EventEnum.ISC_PERSON_GET.getCode().equals(request.getEventType())) {
						personGetCalls[0]++;
						if (personGetCalls[0] < 3) {
							return Result.success(emptyPersonList());
						}
						return Result.success(staffPersonListWithDeletedFirst("deleted-person", "isc-person-current", "JA26079"));
					}
					if (EventEnum.ISC_PERSON_ADD.getCode().equals(request.getEventType())) {
						return Result.success("{}");
					}
					if (EventEnum.ISC_AUTH_CONFIG_ADD.getCode().equals(request.getEventType())) {
						return Result.success("{\"taskId\":\"config-task-current\"}");
					}
					return Result.success("{}");
				});

		service.downAccess();

		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(5)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		DispatcherDTO authRequest = captor.getAllValues().stream()
				.filter(request -> EventEnum.ISC_AUTH_CONFIG_ADD.getCode().equals(request.getEventType()))
				.findFirst()
				.orElseThrow(AssertionError::new);
		Map authParams = (Map) authRequest.getData();
		Object[] personDatas = (Object[]) authParams.get("personDatas");
		Map personInfo = (Map) personDatas[0];
		Assert.assertEquals(Collections.singletonList("isc-person-current"), personInfo.get("indexCodes"));
		Assert.assertEquals("isc-person-current", staffTask.getPersonId());
		Assert.assertNotEquals("JA26079", staffTask.getPersonId());
	}

	@Test
	public void downAccessDoesNotUseStaffBadgeWhenAddedPersonIdCannotBeResolved() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtStaffOtherService staffOtherService = Mockito.mock(SmtStaffOtherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService, imageService,
				remoteStaffService, staffOtherService);
		SmtIscDeviceTask staffTask = staffCardImportTask(22L, "device-1", 5000021, "image-current");
		staffTask.setCardNo("2059164347547275265");
		staffTask.setBadge("JA26079");
		SmtStaff staff = staff("2059164347547275265", "JA26079", StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode());
		SmtImage image = new SmtImage();
		image.setImage(new byte[20 * 1024]);
		Mockito.when(taskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(Mockito.eq(DeviceTaskConstants.CARD), Mockito.anyInt())).thenReturn(Collections.emptySet());
		Mockito.when(taskService.getCardDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(Collections.singletonList(staffTask));
		Mockito.when(taskService.getDelayDown(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(staffOtherService.list(Mockito.any())).thenReturn(Collections.singletonList(staff));
		Mockito.when(imageService.getByCode("image-current")).thenReturn(image);
		stubScheduleStaff(remoteStaffService, "JA26079", "411082199108142426");
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenAnswer(invocation -> {
					DispatcherDTO request = invocation.getArgument(0);
					if (EventEnum.ISC_PERSON_ADD.getCode().equals(request.getEventType())) {
						return Result.success("{}");
					}
					return Result.success(emptyPersonList());
				});

		service.downAccess();

		Assert.assertNull(staffTask.getPersonId());
		Assert.assertNotEquals(DeviceTaskStatusEnum.DOING.getCode(), staffTask.getStatus());
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.atLeastOnce()).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Assert.assertFalse(captor.getAllValues().stream()
				.anyMatch(request -> EventEnum.ISC_AUTH_CONFIG_ADD.getCode().equals(request.getEventType())));
	}

	@Test
	public void delAccessResolvesTemporaryPersonIdByCertNoInsteadOfBlankCardDownRecord() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtStaffOtherService staffOtherService = Mockito.mock(SmtStaffOtherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, Mockito.mock(SmtImageService.class),
				Mockito.mock(SmtDeviceService.class), downRecordService, remoteStaffService, staffOtherService);
		SmtIscDeviceTask visitorDeleteTask = visitorTaskWithoutLocalCard(5L, "cert-del");
		visitorDeleteTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		visitorDeleteTask.setPersonId(null);
		visitorDeleteTask.setStartTime(null);
		Mockito.when(taskService.getDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(pageWith(visitorDeleteTask));
		Mockito.when(taskService.getDelayDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(singleVisitorPersonList("person-del", "cert-del")))
				.thenReturn(Result.success("{\"taskId\":\"delete-task-1\"}"));

		service.delAccess();

		Mockito.verify(taskService, Mockito.never()).getDel(Mockito.any(Page.class), Mockito.anyLong(),
				Mockito.eq(DeviceTaskConstants.CAR));
		Mockito.verify(taskService, Mockito.never()).getDelayDel(Mockito.any(Page.class), Mockito.anyLong(),
				Mockito.eq(DeviceTaskConstants.CAR));
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(2)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Map personQueryParams = (Map) captor.getAllValues().get(0).getData();
		Assert.assertEquals("certificateNo", personQueryParams.get("paramName"));
		Assert.assertEquals("cert-del", ((String[]) personQueryParams.get("paramValue"))[0]);
		Map authParams = (Map) captor.getAllValues().get(1).getData();
		Object[] personDatas = (Object[]) authParams.get("personDatas");
		Map personInfo = (Map) personDatas[0];
		Assert.assertEquals(Collections.singletonList("person-del"), personInfo.get("indexCodes"));
		Mockito.verify(downRecordService, Mockito.never()).getOne(Mockito.any());
		verifyNoScheduleStaffLookup(remoteStaffService);
		Mockito.verify(staffOtherService, Mockito.never()).getOne(Mockito.any());
	}

	@Test
	public void delAccessReResolvesStaffPersonIdByCurrentJobNoWhenTaskHasBadgeValue() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, Mockito.mock(SmtImageService.class),
				Mockito.mock(SmtDeviceService.class), downRecordService, remoteStaffService,
				Mockito.mock(SmtStaffOtherService.class));
		SmtIscDeviceTask staffDeleteTask = new SmtIscDeviceTask();
		staffDeleteTask.setId(21L);
		staffDeleteTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		staffDeleteTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		staffDeleteTask.setDeviceType(DeviceTaskConstants.CARD);
		staffDeleteTask.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		staffDeleteTask.setDeviceCode("device-1");
		staffDeleteTask.setCardNo("2059164347547275265");
		staffDeleteTask.setBadge("JA26079");
		staffDeleteTask.setPersonId("JA26079");
		staffDeleteTask.setParkId(5000021);
		Mockito.when(taskService.getDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(pageWith(staffDeleteTask));
		Mockito.when(taskService.getDelayDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		stubScheduleStaff(remoteStaffService, "JA26079", "411082199108142426");
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenAnswer(invocation -> {
					DispatcherDTO request = invocation.getArgument(0);
					if (EventEnum.ISC_PERSON_GET.getCode().equals(request.getEventType())) {
						return Result.success(singleStaffPersonList("isc-person-current", "JA26079", "face-current"));
					}
					if (EventEnum.ISC_AUTH_CONFIG_DEL.getCode().equals(request.getEventType())) {
						return Result.success("{\"taskId\":\"delete-task-current\"}");
					}
					return Result.success("{}");
				});

		service.delAccess();

		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(2)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		DispatcherDTO authRequest = captor.getAllValues().stream()
				.filter(request -> EventEnum.ISC_AUTH_CONFIG_DEL.getCode().equals(request.getEventType()))
				.findFirst()
				.orElseThrow(AssertionError::new);
		Map authParams = (Map) authRequest.getData();
		Object[] personDatas = (Object[]) authParams.get("personDatas");
		Map personInfo = (Map) personDatas[0];
		Assert.assertEquals(Collections.singletonList("isc-person-current"), personInfo.get("indexCodes"));
		Assert.assertEquals("isc-person-current", staffDeleteTask.getPersonId());
	}

	@Test
	public void delAccessKeepsOriginalDownRecordPersonIdWhenCurrentJobNoResolvesDifferentPerson() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, Mockito.mock(SmtImageService.class),
				Mockito.mock(SmtDeviceService.class), downRecordService, remoteStaffService,
				Mockito.mock(SmtStaffOtherService.class));
		SmtIscDeviceTask staffDeleteTask = new SmtIscDeviceTask();
		staffDeleteTask.setId(23L);
		staffDeleteTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		staffDeleteTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		staffDeleteTask.setDeviceType(DeviceTaskConstants.CARD);
		staffDeleteTask.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		staffDeleteTask.setDeviceCode("device-1");
		staffDeleteTask.setCardNo("2059164347547275265");
		staffDeleteTask.setBadge("JA26079");
		staffDeleteTask.setPersonId("JA26079");
		staffDeleteTask.setParkId(5000021);
		SmtIscDownRecord downRecord = new SmtIscDownRecord();
		downRecord.setCardNo("2059164347547275265");
		downRecord.setBadge("JA26079");
		downRecord.setPersonId("isc-person-old");
		Mockito.when(taskService.getDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(pageWith(staffDeleteTask));
		Mockito.when(taskService.getDelayDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(downRecordService.getOne(Mockito.any())).thenReturn(downRecord);
		stubScheduleStaff(remoteStaffService, "JA26079", "411082199108142426");
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenAnswer(invocation -> {
					DispatcherDTO request = invocation.getArgument(0);
					if (EventEnum.ISC_PERSON_GET.getCode().equals(request.getEventType())) {
						return Result.success(singleStaffPersonList("isc-person-current", "JA26079", "face-current"));
					}
					if (EventEnum.ISC_AUTH_CONFIG_DEL.getCode().equals(request.getEventType())) {
						return Result.success("{\"taskId\":\"delete-task-old\"}");
					}
					return Result.success("{}");
				});

		service.delAccess();

		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(1)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		DispatcherDTO authRequest = captor.getValue();
		Assert.assertEquals(EventEnum.ISC_AUTH_CONFIG_DEL.getCode(), authRequest.getEventType());
		Map authParams = (Map) authRequest.getData();
		Object[] personDatas = (Object[]) authParams.get("personDatas");
		Map personInfo = (Map) personDatas[0];
		Assert.assertEquals(Collections.singletonList("isc-person-old"), personInfo.get("indexCodes"));
		Assert.assertEquals("isc-person-old", staffDeleteTask.getPersonId());
	}

	@Test
	public void delAccessReResolvesBadgePersonIdWhenTaskAndDownRecordBadgesAreBlank() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, Mockito.mock(SmtImageService.class),
				Mockito.mock(SmtDeviceService.class), downRecordService, remoteStaffService,
				Mockito.mock(SmtStaffOtherService.class));
		SmtIscDeviceTask staffDeleteTask = new SmtIscDeviceTask();
		staffDeleteTask.setId(24L);
		staffDeleteTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		staffDeleteTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		staffDeleteTask.setDeviceType(DeviceTaskConstants.CARD);
		staffDeleteTask.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		staffDeleteTask.setDeviceCode("device-1");
		staffDeleteTask.setCardNo("2059164347547275265");
		staffDeleteTask.setGeneral("JA26079-张倩瑜");
		staffDeleteTask.setPersonId(null);
		staffDeleteTask.setParkId(5000021);
		SmtIscDownRecord downRecord = new SmtIscDownRecord();
		downRecord.setCardNo("2059164347547275265");
		downRecord.setGeneral("JA26079-张倩瑜");
		downRecord.setPersonId("JA26079");
		Mockito.when(taskService.getDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(pageWith(staffDeleteTask));
		Mockito.when(taskService.getDelayDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(downRecordService.getOne(Mockito.any())).thenReturn(downRecord);
		stubScheduleStaff(remoteStaffService, "JA26079", "411082199108142426");
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenAnswer(invocation -> {
					DispatcherDTO request = invocation.getArgument(0);
					if (EventEnum.ISC_PERSON_GET.getCode().equals(request.getEventType())) {
						return Result.success(singleStaffPersonList("isc-person-current", "JA26079", "face-current"));
					}
					if (EventEnum.ISC_AUTH_CONFIG_DEL.getCode().equals(request.getEventType())) {
						return Result.success("{\"taskId\":\"delete-task-current\"}");
					}
					return Result.success("{}");
				});

		service.delAccess();

		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(2)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		DispatcherDTO authRequest = captor.getAllValues().stream()
				.filter(request -> EventEnum.ISC_AUTH_CONFIG_DEL.getCode().equals(request.getEventType()))
				.findFirst()
				.orElseThrow(AssertionError::new);
		Map authParams = (Map) authRequest.getData();
		Object[] personDatas = (Object[]) authParams.get("personDatas");
		Map personInfo = (Map) personDatas[0];
		Assert.assertEquals(Collections.singletonList("isc-person-current"), personInfo.get("indexCodes"));
		Assert.assertEquals("isc-person-current", staffDeleteTask.getPersonId());
	}

	@Test
	public void delAccessLooksUpDownRecordByDeviceTypeAndServiceType() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, Mockito.mock(SmtImageService.class),
				Mockito.mock(SmtDeviceService.class), downRecordService);
		SmtIscDeviceTask staffDeleteTask = new SmtIscDeviceTask();
		staffDeleteTask.setId(11L);
		staffDeleteTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		staffDeleteTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		staffDeleteTask.setDeviceType(DeviceTaskConstants.CARD);
		staffDeleteTask.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		staffDeleteTask.setDeviceCode("device-1");
		staffDeleteTask.setCardNo("1001");
		staffDeleteTask.setParkId(5000021);
		SmtIscDownRecord downRecord = new SmtIscDownRecord();
		downRecord.setPersonId("person-staff");
		Mockito.when(taskService.getDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(pageWith(staffDeleteTask));
		Mockito.when(taskService.getDelayDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(downRecordService.getOne(Mockito.any())).thenReturn(downRecord);
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success("{\"taskId\":\"delete-task-staff\"}"));

		service.delAccess();

		ArgumentCaptor<LambdaQueryWrapper> downRecordQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(downRecordService).getOne(downRecordQueryCaptor.capture());
		Assert.assertTrue(queryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD));
		Assert.assertTrue(queryHasParam(downRecordQueryCaptor.getValue(), DeviceTaskConstants.CARD_STAFF_IMPORT));
		ArgumentCaptor<DispatcherDTO> dispatcherCaptor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService).dispatch(dispatcherCaptor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Map authParams = (Map) dispatcherCaptor.getValue().getData();
		Object[] personDatas = (Object[]) authParams.get("personDatas");
		Map personInfo = (Map) personDatas[0];
		Assert.assertEquals(Collections.singletonList("person-staff"), personInfo.get("indexCodes"));
	}

	@Test
	public void delAccessKeepsCardDeleteRecordsWithoutVehicleQueries() throws Exception {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, Mockito.mock(SmtImageService.class),
				Mockito.mock(SmtDeviceService.class), downRecordService);
		setOptionalField(service, "iscVehicleAuthEnabled", true);
		SmtIscDeviceTask staffDeleteTask = new SmtIscDeviceTask();
		staffDeleteTask.setId(12L);
		staffDeleteTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		staffDeleteTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		staffDeleteTask.setDeviceType(DeviceTaskConstants.CARD);
		staffDeleteTask.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		staffDeleteTask.setDeviceCode("device-1");
		staffDeleteTask.setCardNo("1001");
		staffDeleteTask.setParkId(5000021);
		SmtIscDownRecord downRecord = new SmtIscDownRecord();
		downRecord.setPersonId("person-staff");
		Mockito.when(taskService.getDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenAnswer(invocation -> incomingPageWithRecords(invocation.getArgument(0),
						Collections.singletonList(staffDeleteTask)));
		Mockito.when(taskService.getDelayDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenAnswer(invocation -> incomingPageWithRecords(invocation.getArgument(0), Collections.emptyList()));
		Mockito.when(downRecordService.getOne(Mockito.any())).thenReturn(downRecord);
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success("{\"taskId\":\"delete-task-staff\"}"));

		service.delAccess();

		ArgumentCaptor<DispatcherDTO> dispatcherCaptor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService).dispatch(dispatcherCaptor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Map authParams = (Map) dispatcherCaptor.getValue().getData();
		Object[] personDatas = (Object[]) authParams.get("personDatas");
		Map personInfo = (Map) personDatas[0];
		Assert.assertEquals(Collections.singletonList("person-staff"), personInfo.get("indexCodes"));
		Mockito.verify(taskService, Mockito.never()).getDel(Mockito.any(Page.class), Mockito.anyLong(),
				Mockito.eq(DeviceTaskConstants.CAR));
		Mockito.verify(taskService, Mockito.never()).getDelayDel(Mockito.any(Page.class), Mockito.anyLong(),
				Mockito.eq(DeviceTaskConstants.CAR));
	}

	@Test
	public void handelVisitorGeneratesDistinctDeleteTasksForBlankCardVisitorsByPersonId() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService,
				Mockito.mock(SmtImageService.class));
		SmtIscDeviceTask firstVisitor = visitorTaskWithoutLocalCard(6L, "cert-gen-1");
		firstVisitor.setPersonId("person-gen-1");
		SmtIscDeviceTask secondVisitor = visitorTaskWithoutLocalCard(7L, "cert-gen-2");
		secondVisitor.setPersonId("person-gen-2");
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(taskService.save(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);

		service.handelVisitor(firstVisitor);
		service.handelVisitor(secondVisitor);

		ArgumentCaptor<SmtIscDeviceTask> captor = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(taskService, Mockito.times(2)).save(captor.capture());
		Assert.assertEquals("person-gen-1", captor.getAllValues().get(0).getPersonId());
		Assert.assertEquals("person-gen-2", captor.getAllValues().get(1).getPersonId());
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), captor.getAllValues().get(0).getAction());
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), captor.getAllValues().get(1).getAction());
		Assert.assertNull(captor.getAllValues().get(0).getCardNo());
		Assert.assertNull(captor.getAllValues().get(1).getCardNo());
		Assert.assertEquals(Long.valueOf(TEMP_ACCESS_END), captor.getAllValues().get(0).getOverTime());
		Assert.assertEquals(Long.valueOf(TEMP_ACCESS_END), captor.getAllValues().get(1).getOverTime());
	}

	@Test
	public void handelVisitorResetsRetryTimesWhenGeneratingDeleteTaskAfterFinalSuccess() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService,
				Mockito.mock(SmtImageService.class));
		SmtIscDeviceTask visitor = visitorTaskWithoutLocalCard(11L, "cert-gen-max-retry");
		visitor.setPersonId("person-gen-max-retry");
		visitor.setTimes(DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES);
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(taskService.save(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);

		service.handelVisitor(visitor);

		ArgumentCaptor<SmtIscDeviceTask> captor = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(taskService).save(captor.capture());
		SmtIscDeviceTask generatedDeleteTask = captor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), generatedDeleteTask.getAction());
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), generatedDeleteTask.getStatus());
		Assert.assertNull(generatedDeleteTask.getTimes());
	}

	@Test
	public void handelVisitorUpdatesExistingDoingTemporaryDeleteTaskToLatestEndTime() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService,
				Mockito.mock(SmtImageService.class));
		SmtIscDeviceTask visitor = visitorTaskWithoutLocalCard(8L, "cert-gen-merge");
		visitor.setPersonId("person-gen-merge");
		SmtIscDeviceTask existingDeleteTask = visitorTaskWithoutLocalCard(9L, "cert-gen-merge");
		existingDeleteTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		existingDeleteTask.setStatus(DeviceTaskStatusEnum.DOING.getCode());
		existingDeleteTask.setPersonId("person-gen-merge");
		existingDeleteTask.setOverTime(TEMP_ACCESS_START + 3600);
		existingDeleteTask.setCode(202);
		existingDeleteTask.setIscTaskId("download-task-merge");
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(existingDeleteTask));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);

		service.handelVisitor(visitor);

		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(taskService).list(queryCaptor.capture());
		Assert.assertTrue(queryHasParam(queryCaptor.getValue(), DeviceTaskStatusEnum.DOING.getCode()));
		Mockito.verify(taskService, Mockito.never()).save(Mockito.any(SmtIscDeviceTask.class));
		Mockito.verify(taskService).updateById(existingDeleteTask);
		Assert.assertEquals(Long.valueOf(TEMP_ACCESS_END), existingDeleteTask.getOverTime());
		Assert.assertEquals(DeviceTaskStatusEnum.DOING.getCode(), existingDeleteTask.getStatus());
		Assert.assertEquals(Integer.valueOf(202), existingDeleteTask.getCode());
		Assert.assertEquals("download-task-merge", existingDeleteTask.getIscTaskId());
	}

	@Test
	public void handelVisitorSkipsVehicleAdmittanceDeleteTask() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		ISCDeviceTaskServiceImpl service = serviceForDownAccess(dispatcherService, taskService,
				Mockito.mock(SmtImageService.class));
		SmtIscDeviceTask admittanceVehicleTask = new SmtIscDeviceTask();
		admittanceVehicleTask.setId(10L);
		admittanceVehicleTask.setAction(DeviceTaskActionEnum.DOWN.getCode());
		admittanceVehicleTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		admittanceVehicleTask.setDeviceType(DeviceTaskConstants.CAR);
		admittanceVehicleTask.setServiceType(DeviceTaskConstants.CAT_ADMITTANCE);
		admittanceVehicleTask.setDeviceCode("car-device-1");
		admittanceVehicleTask.setCardNo("4001");
		admittanceVehicleTask.setGeneral("粤B12345");
		admittanceVehicleTask.setStartTime(TEMP_ACCESS_START);
		admittanceVehicleTask.setOverTime(TEMP_ACCESS_END);

		service.handelVisitor(admittanceVehicleTask);

		Mockito.verify(taskService, Mockito.never()).list(Mockito.any());
		Mockito.verify(taskService, Mockito.never()).save(Mockito.any(SmtIscDeviceTask.class));
	}

	private boolean queryHasParam(LambdaQueryWrapper queryWrapper, Object expected) {
		return wrapperHasParam(queryWrapper, expected);
	}

	private boolean wrapperHasParam(AbstractWrapper wrapper, Object expected) {
		wrapper.getSqlSegment();
		return wrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> queryParamMatches(value, expected));
	}

	private boolean queryParamMatches(Object value, Object expected) {
		if (value != null && value.getClass().isArray()) {
			int length = Array.getLength(value);
			for (int i = 0; i < length; i++) {
				if (String.valueOf(expected).equals(String.valueOf(Array.get(value, i)))) {
					return true;
				}
			}
			return false;
		}
		if (value instanceof Iterable) {
			for (Object item : (Iterable<?>) value) {
				if (String.valueOf(expected).equals(String.valueOf(item))) {
					return true;
				}
			}
			return false;
		}
		return String.valueOf(expected).equals(String.valueOf(value));
	}

	private SmtStaff staff(String id, String badge, Integer status) {
		SmtStaff staff = new SmtStaff();
		staff.setId(Long.valueOf(id));
		staff.setBadge(badge);
		staff.setStatus(status);
		return staff;
	}

	@Test
	public void delAccessMarksDelayedDeleteSuccessWhenIscPersonGoneByBothIdentifiers() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, Mockito.mock(SmtImageService.class),
				Mockito.mock(SmtDeviceService.class), downRecordService, remoteStaffService,
				Mockito.mock(SmtStaffOtherService.class));
		SmtIscDeviceTask task = staffDeleteTaskWithoutPersonId(DeviceTaskActionEnum.DELAY_DEL.getCode());
		Mockito.when(taskService.getDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.getDelayDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(pageWith(task));
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		stubScheduleStaff(remoteStaffService, "JA26079", "411082199108142426");
		// ISC按工号、证件号查询均为空：人员已被ISC删除，权限已级联清理
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success("{\"list\":[]}"));

		service.delAccess();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("ISC人员已删除"));
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.atLeastOnce()).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Assert.assertFalse(captor.getAllValues().stream()
				.anyMatch(request -> EventEnum.ISC_AUTH_CONFIG_DEL.getCode().equals(request.getEventType())));
	}

	@Test
	public void delAccessMarksDeleteSuccessWhenIscPersonOnlyExistsInDeletedState() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, Mockito.mock(SmtImageService.class),
				Mockito.mock(SmtDeviceService.class), downRecordService, remoteStaffService,
				Mockito.mock(SmtStaffOtherService.class));
		SmtIscDeviceTask task = staffDeleteTaskWithoutPersonId(DeviceTaskActionEnum.DEL.getCode());
		Mockito.when(taskService.getDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(pageWith(task));
		Mockito.when(taskService.getDelayDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		stubScheduleStaff(remoteStaffService, "JA26079", "411082199108142426");
		// ISC返回的人员全部处于删除状态（status<0）：等同人员已删除
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(deletedOnlyPersonList("isc-person-deleted", "JA26079")));

		service.delAccess();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), task.getCode());
		Assert.assertTrue(task.getRemark().contains("ISC人员已删除"));
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
	}

	@Test
	public void delAccessKeepsRetryWhenIscPersonQueryFails() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, Mockito.mock(SmtImageService.class),
				Mockito.mock(SmtDeviceService.class), downRecordService, remoteStaffService,
				Mockito.mock(SmtStaffOtherService.class));
		SmtIscDeviceTask task = staffDeleteTaskWithoutPersonId(DeviceTaskActionEnum.DEL.getCode());
		Mockito.when(taskService.getDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(pageWith(task));
		Mockito.when(taskService.getDelayDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		stubScheduleStaff(remoteStaffService, "JA26079", "411082199108142426");
		// ISC查询失败：不能判定人员已删除，必须保留重试
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.fail("ISC平台查询超时"));

		service.delAccess();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertTrue(task.getRemark().contains("删除任务保留并将在1小时后重试"));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any());
	}

	@Test
	public void delAccessResolvesPersonByCertNoWhenJobNoLookupIsEmpty() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, Mockito.mock(SmtImageService.class),
				deviceService, downRecordService, remoteStaffService, Mockito.mock(SmtStaffOtherService.class));
		SmtIscDeviceTask task = staffDeleteTaskWithoutPersonId(DeviceTaskActionEnum.DEL.getCode());
		Mockito.when(taskService.getDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(pageWith(task));
		Mockito.when(taskService.getDelayDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		Mockito.when(deviceService.getById("device-1")).thenReturn(device());
		stubScheduleStaff(remoteStaffService, "JA26079", "411082199108142426");
		// 工号查询为空，但证件号复查命中在册人员：不能误判删除，按正常删除流程下发
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenAnswer(invocation -> {
					DispatcherDTO request = invocation.getArgument(0);
					if (EventEnum.ISC_PERSON_GET.getCode().equals(request.getEventType())) {
						Map params = (Map) request.getData();
						if ("certificateNo".equals(params.get("paramName"))) {
							return Result.success(singleVisitorPersonList("isc-person-x", "411082199108142426"));
						}
						return Result.success("{\"list\":[]}");
					}
					if (EventEnum.ISC_AUTH_CONFIG_DEL.getCode().equals(request.getEventType())) {
						return Result.success("{\"taskId\":\"del-config-1\"}");
					}
					return Result.success("{}");
				});

		service.delAccess();

		Assert.assertEquals(DeviceTaskStatusEnum.DOING.getCode(), task.getStatus());
		Assert.assertEquals("isc-person-x", task.getPersonId());
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.atLeastOnce()).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Assert.assertTrue(captor.getAllValues().stream()
				.anyMatch(request -> EventEnum.ISC_AUTH_CONFIG_DEL.getCode().equals(request.getEventType())));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any());
	}

	@Test
	public void delAccessKeepsRetryWhenIscPersonRecordsMissPersonId() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, Mockito.mock(SmtImageService.class),
				Mockito.mock(SmtDeviceService.class), downRecordService, remoteStaffService,
				Mockito.mock(SmtStaffOtherService.class));
		SmtIscDeviceTask task = staffDeleteTaskWithoutPersonId(DeviceTaskActionEnum.DEL.getCode());
		Mockito.when(taskService.getDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(pageWith(task));
		Mockito.when(taskService.getDelayDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.update(Mockito.any())).thenReturn(true);
		stubScheduleStaff(remoteStaffService, "JA26079", "411082199108142426");
		// ISC返回了记录但personId缺失：疑似响应结构异常，不得误判人员已删除
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success("{\"list\":[{\"jobNo\":\"JA26079\",\"status\":1}]}"));

		service.delAccess();

		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertTrue(task.getRemark().contains("删除任务保留并将在1小时后重试"));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any());
	}

	@Test
	public void delAccessMarksDeleteSuccessByCertNoOnlyWhenLocalBadgeMissing() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		ISCDeviceTaskServiceImpl service = service(dispatcherService, taskService, Mockito.mock(SmtImageService.class),
				Mockito.mock(SmtDeviceService.class), downRecordService, remoteStaffService,
				Mockito.mock(SmtStaffOtherService.class));
		SmtIscDeviceTask task = staffDeleteTaskWithoutPersonId(DeviceTaskActionEnum.DEL.getCode());
		Mockito.when(taskService.getDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(pageWith(task));
		Mockito.when(taskService.getDelayDel(Mockito.any(Page.class), Mockito.anyLong(), Mockito.eq(DeviceTaskConstants.CARD)))
				.thenReturn(new Page<>());
		Mockito.when(taskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(true);
		// 本地员工记录没有工号：只能按证件号单标识判定
		stubScheduleStaff(remoteStaffService, null, "411082199108142426");
		Mockito.when(dispatcherService.dispatch(Mockito.any(DispatcherDTO.class), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success("{\"list\":[]}"));

		service.delAccess();

		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertTrue(task.getRemark().contains("ISC人员已删除"));
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
		// 只发生证件号查询，不应出现工号查询
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.atLeastOnce()).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Assert.assertTrue(captor.getAllValues().stream()
				.filter(request -> EventEnum.ISC_PERSON_GET.getCode().equals(request.getEventType()))
				.allMatch(request -> "certificateNo".equals(((Map) request.getData()).get("paramName"))));
	}

	private SmtIscDeviceTask staffDeleteTaskWithoutPersonId(Integer action) {
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setId(7L);
		task.setAction(action);
		task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		task.setDeviceCode("device-1");
		task.setDeviceType(DeviceTaskConstants.CARD);
		task.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		task.setCardNo("2059164347547275265");
		task.setBadge("JA26079");
		task.setPersonId(null);
		task.setParkId(5000021);
		task.setCreateTime(LocalDateTime.now());
		return task;
	}

	private String deletedOnlyPersonList(String personId, String jobNo) {
		Map<String, Object> person = new HashMap<>();
		person.put("personId", personId);
		person.put("jobNo", jobNo);
		person.put("certificateNo", "411082199108142426");
		person.put("status", -1);
		person.put("personPhoto", Collections.emptyList());
		Map<String, Object> body = new HashMap<>();
		body.put("list", Collections.singletonList(person));
		return JSONUtil.toJsonStr(body);
	}

	/**
	 * 统一声明 Smart Schedule 需要的两类内部员工资料，防止测试回退到通用员工实体契约。
	 */
	private void stubScheduleStaff(RemoteStaffService remoteStaffService, String badge, String certNo) {
		Mockito.when(remoteStaffService.getScheduleIdentityStaff("2059164347547275265", SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED))
				.thenReturn(Result.success(scheduleIdentityStaff(badge, certNo)));
		Mockito.when(remoteStaffService.getScheduleIscPersonStaff("2059164347547275265", SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED))
				.thenReturn(Result.success(scheduleIscPersonStaff(badge, certNo)));
	}

	private InternalScheduleStaffIdentityRespDTO scheduleIdentityStaff(String badge, String certNo) {
		InternalScheduleStaffIdentityRespDTO staff = new InternalScheduleStaffIdentityRespDTO();
		staff.setBadge(badge);
		staff.setCertno(certNo);
		staff.setStatus(0);
		return staff;
	}

	private InternalScheduleIscPersonRespDTO scheduleIscPersonStaff(String badge, String certNo) {
		InternalScheduleIscPersonRespDTO staff = new InternalScheduleIscPersonRespDTO();
		staff.setBadge(badge);
		staff.setName("张倩瑜");
		staff.setSex(1);
		staff.setCertno(certNo);
		return staff;
	}

	private void verifyNoScheduleStaffLookup(RemoteStaffService remoteStaffService) {
		Mockito.verify(remoteStaffService, Mockito.never()).getScheduleIdentityStaff(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString());
		Mockito.verify(remoteStaffService, Mockito.never()).getScheduleIscPersonStaff(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString());
	}

	private ISCDeviceTaskServiceImpl service(RemoteDispatcherService dispatcherService,
											SmtIscDeviceTaskService taskService,
											SmtDeviceService deviceService,
											SmtIscDownRecordService downRecordService) {
		return service(dispatcherService, taskService, Mockito.mock(SmtImageService.class), deviceService,
				downRecordService);
	}

	private ISCDeviceTaskServiceImpl serviceForDownAccess(RemoteDispatcherService dispatcherService,
														SmtIscDeviceTaskService taskService,
														SmtImageService imageService) {
		return service(dispatcherService, taskService, imageService, Mockito.mock(SmtDeviceService.class),
				Mockito.mock(SmtIscDownRecordService.class));
	}

	/**
	 * 批量终态补聚合钩子测试专用：注入可验证的SmtAdmittanceApplyMapper，
	 * 通过selectById(applyId)调用断言聚合被逐单触发（aggregate的第一步就是查申请单）。
	 */
	private ISCDeviceTaskServiceImpl serviceForDownAccess(RemoteDispatcherService dispatcherService,
														SmtIscDeviceTaskService taskService,
														SmtImageService imageService,
														SmtAdmittanceApplyMapper admittanceApplyMapper) {
		return new ISCDeviceTaskServiceImpl(
				Mockito.mock(RemoteStaffService.class),
				dispatcherService,
				imageService,
				taskService,
				admittanceApplyMapper,
				Mockito.mock(SmtAdmittanceFellowMapper.class),
				Mockito.mock(SmtDeviceService.class),
				Mockito.mock(SmtIscDownRecordService.class),
				Mockito.mock(RemoteParkInternalService.class),
				Mockito.mock(SmtVisitorService.class),
				Mockito.mock(SmtFellowVisitorMapper.class),
				Mockito.mock(SmtAdmittanceFellowMapper.class),
				Mockito.mock(RemoteSnapPersonService.class),
				Mockito.mock(SmtMsgTempService.class),
				Mockito.mock(SmtStaffOtherService.class),
				Mockito.mock(StringRedisTemplate.class));
	}

	private ISCDeviceTaskServiceImpl serviceForDownAccess(RemoteDispatcherService dispatcherService,
														SmtIscDeviceTaskService taskService,
														SmtImageService imageService,
														RemoteStaffService remoteStaffService,
														SmtStaffOtherService staffOtherService) {
		return service(dispatcherService, taskService, imageService, Mockito.mock(SmtDeviceService.class),
				Mockito.mock(SmtIscDownRecordService.class), remoteStaffService, staffOtherService);
	}

	private ISCDeviceTaskServiceImpl service(RemoteDispatcherService dispatcherService,
											SmtIscDeviceTaskService taskService,
											SmtImageService imageService,
											SmtDeviceService deviceService,
											SmtIscDownRecordService downRecordService) {
		return service(dispatcherService, taskService, imageService, deviceService, downRecordService,
				Mockito.mock(RemoteStaffService.class), Mockito.mock(SmtStaffOtherService.class));
	}

	private ISCDeviceTaskServiceImpl service(RemoteDispatcherService dispatcherService,
											SmtIscDeviceTaskService taskService,
											SmtImageService imageService,
											SmtDeviceService deviceService,
											SmtIscDownRecordService downRecordService,
											RemoteStaffService remoteStaffService,
											SmtStaffOtherService staffOtherService) {
		return new ISCDeviceTaskServiceImpl(
				remoteStaffService,
				dispatcherService,
				imageService,
				taskService,
				Mockito.mock(SmtAdmittanceApplyMapper.class),
				Mockito.mock(SmtAdmittanceFellowMapper.class),
				deviceService,
				downRecordService,
				Mockito.mock(RemoteParkInternalService.class),
				Mockito.mock(SmtVisitorService.class),
				Mockito.mock(SmtFellowVisitorMapper.class),
				Mockito.mock(SmtAdmittanceFellowMapper.class),
				Mockito.mock(RemoteSnapPersonService.class),
				Mockito.mock(SmtMsgTempService.class),
				staffOtherService,
				Mockito.mock(StringRedisTemplate.class));
	}

	private SmtIscDeviceTask visitorTaskWithoutLocalCard(Long id, String certNo) {
		return visitorTask(id, null, certNo);
	}

	private SmtIscDeviceTask visitorTask(Long id, String cardNo, String certNo) {
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setId(id);
		task.setAction(DeviceTaskActionEnum.DOWN.getCode());
		task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		task.setDeviceType(DeviceTaskConstants.CARD);
		task.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		task.setDeviceCode("device-1");
		task.setCardNo(cardNo);
		task.setBadge(certNo);
		task.setImageId("image-" + id);
		task.setStartTime(TEMP_ACCESS_START);
		task.setOverTime(TEMP_ACCESS_END);
		task.setParkId(5000021);
		task.setCreateTime(LocalDateTime.now());
		return task;
	}

	private SmtIscDeviceTask staffUpdateFaceTask() {
		return staffUpdateFaceTask(15L, "device-2", 5000022, "image-staff");
	}

	private SmtIscDeviceTask staffUpdateFaceTask(Long id, String deviceCode, Integer parkId, String imageId) {
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setId(id);
		task.setAction(DeviceTaskActionEnum.UPDATE.getCode());
		task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		task.setDeviceType(DeviceTaskConstants.CARD);
		task.setServiceType(DeviceTaskConstants.UPDATE_FACE);
		task.setDeviceCode(deviceCode);
		task.setCardNo("1001");
		task.setBadge("JA26086");
		task.setImageId(imageId);
		task.setStartTime(TEMP_ACCESS_START);
		task.setOverTime(DeviceTaskConstants.maxTime);
		task.setParkId(parkId);
		task.setCreateTime(LocalDateTime.now());
		return task;
	}

	private SmtIscDeviceTask staffCardImportTask(Long id, String deviceCode, Integer parkId, String imageId) {
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setId(id);
		task.setAction(DeviceTaskActionEnum.DOWN.getCode());
		task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		task.setDeviceType(DeviceTaskConstants.CARD);
		task.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		task.setDeviceCode(deviceCode);
		task.setCardNo("1001");
		task.setBadge("JA26086");
		task.setImageId(imageId);
		task.setStartTime(TEMP_ACCESS_START);
		task.setOverTime(DeviceTaskConstants.maxTime);
		task.setParkId(parkId);
		task.setCreateTime(LocalDateTime.now());
		return task;
	}

	private String visitorPersonList() {
		Map<String, Object> firstPerson = visitorPerson("person-1", "cert-1");
		Map<String, Object> secondPerson = visitorPerson("person-2", "cert-2");
		Map<String, Object> body = new HashMap<>();
		body.put("list", Arrays.asList(firstPerson, secondPerson));
		return JSONUtil.toJsonStr(body);
	}

	private String singleVisitorPersonList(String personId, String certNo) {
		Map<String, Object> body = new HashMap<>();
		body.put("list", Collections.singletonList(visitorPerson(personId, certNo)));
		return JSONUtil.toJsonStr(body);
	}

	private String singleStaffPersonList(String personId, String jobNo, String faceId) {
		Map<String, Object> photo = new HashMap<>();
		photo.put("personPhotoIndexCode", faceId);
		Map<String, Object> person = new HashMap<>();
		person.put("personId", personId);
		person.put("jobNo", jobNo);
		person.put("status", 1);
		person.put("personPhoto", Collections.singletonList(photo));
		Map<String, Object> body = new HashMap<>();
		body.put("list", Collections.singletonList(person));
		return JSONUtil.toJsonStr(body);
	}

	private String staffPersonListWithDeletedFirst(String deletedPersonId, String activePersonId, String jobNo) {
		Map<String, Object> deletedPerson = new HashMap<>();
		deletedPerson.put("personId", deletedPersonId);
		deletedPerson.put("jobNo", jobNo);
		deletedPerson.put("status", -1);
		deletedPerson.put("personPhoto", Collections.emptyList());
		Map<String, Object> activePerson = JSONUtil.parseObj(singleStaffPersonList(activePersonId, jobNo, "face-current"))
				.getJSONArray("list")
				.getJSONObject(0);
		Map<String, Object> body = new HashMap<>();
		body.put("list", Arrays.asList(deletedPerson, activePerson));
		return JSONUtil.toJsonStr(body);
	}

	private String singleStaffPersonListWithoutFace(String personId, String jobNo) {
		Map<String, Object> person = new HashMap<>();
		person.put("personId", personId);
		person.put("jobNo", jobNo);
		person.put("status", 1);
		person.put("personPhoto", Collections.emptyList());
		Map<String, Object> body = new HashMap<>();
		body.put("list", Collections.singletonList(person));
		return JSONUtil.toJsonStr(body);
	}

	private String emptyPersonList() {
		Map<String, Object> body = new HashMap<>();
		body.put("list", Collections.emptyList());
		return JSONUtil.toJsonStr(body);
	}

	private Page<SmtIscDeviceTask> pageWith(SmtIscDeviceTask task) {
		Page<SmtIscDeviceTask> page = new Page<>();
		page.setRecords(Collections.singletonList(task));
		return page;
	}

	private Page<SmtIscDeviceTask> incomingPageWithRecords(Page<SmtIscDeviceTask> page,
														   List<SmtIscDeviceTask> records) {
		page.setRecords(records);
		return page;
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private void setOptionalField(Object target, String name, Object value) throws Exception {
		try {
			setField(target, name, value);
		} catch (NoSuchFieldException ignored) {
			// The legacy field may be removed; the behavior must stay disabled either way.
		}
	}

	private Map<String, Object> visitorPerson(String personId, String certNo) {
		Map<String, Object> photo = new HashMap<>();
		photo.put("personPhotoIndexCode", "face-" + personId);
		Map<String, Object> person = new HashMap<>();
		person.put("personId", personId);
		person.put("certificateNo", certNo);
		person.put("status", 1);
		person.put("personPhoto", Collections.singletonList(photo));
		return person;
	}

	private SmtIscDeviceTask deleteTask() {
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setId(1L);
		task.setAction(DeviceTaskActionEnum.DEL.getCode());
		task.setStatus(DeviceTaskStatusEnum.DOING.getCode());
		task.setCode(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_OK.getCode());
		task.setIscTaskId("download-task-1");
		task.setDeviceCode("device-1");
		task.setDeviceType(1);
		task.setCardNo("card-1");
		task.setPersonId("person-1");
		return task;
	}

	private SmtIscDeviceTask downTask() {
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setId(2L);
		task.setAction(DeviceTaskActionEnum.DOWN.getCode());
		task.setStatus(DeviceTaskStatusEnum.DOING.getCode());
		task.setCode(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_OK.getCode());
		task.setIscTaskId("download-task-1");
		task.setDeviceCode("device-1");
		task.setDeviceType(1);
		task.setCardNo("card-2");
		task.setPersonId("person-2");
		return task;
	}

	private SmtDevice device() {
		SmtDevice device = new SmtDevice();
		device.setId("device-1");
		device.setParkId(5000021);
		device.setChannelNo(1);
		return device;
	}

	private String downloadProgress(String errorCode) {
		Map<String, Object> progress = new HashMap<>();
		progress.put("isDownloadFinished", true);
		progress.put("downloadResult", 1);
		progress.put("errorCode", errorCode);
		Map<String, Object> body = new HashMap<>();
		body.put("isDownloadFinished", true);
		body.put("resourceDownloadProgress", Collections.singletonList(progress));
		return JSONUtil.toJsonStr(body);
	}

	private String partialDownloadProgress(String errorCode) {
		Map<String, Object> progress = new HashMap<>();
		progress.put("isDownloadFinished", true);
		progress.put("downloadResult", 2);
		progress.put("errorCode", errorCode);
		Map<String, Object> body = new HashMap<>();
		body.put("isDownloadFinished", true);
		body.put("resourceDownloadProgress", Collections.singletonList(progress));
		return JSONUtil.toJsonStr(body);
	}

	private String downloadDetailForOtherPerson() {
		return downloadDetailForPerson("other-person", "1");
	}

	private String downloadDetailForPerson(String personId, String resultCode) {
		Map<String, Object> person = new HashMap<>();
		person.put("errorCode", "0");
		Map<String, Object> face = new HashMap<>();
		face.put("errorCode", "0");
		Map<String, Object> personDownloadDetail = new HashMap<>();
		personDownloadDetail.put("person", person);
		personDownloadDetail.put("faces", Collections.singletonList(face));
		Map<String, Object> detail = new HashMap<>();
		detail.put("personId", personId);
		detail.put("persondownloadResult", resultCode);
		detail.put("personDownloadDetail", personDownloadDetail);
		Map<String, Object> body = new HashMap<>();
		body.put("total", 1);
		body.put("list", Collections.singletonList(detail));
		return JSONUtil.toJsonStr(body);
	}

	private String downloadDetailWithNestedErrors(String personId) {
		Map<String, Object> person = new HashMap<>();
		person.put("errorCode", "0");
		Map<String, Object> card = new HashMap<>();
		card.put("errorCode", ISCDeviceTaskErrorEnum.CALLBACK_CARD_NUMBER_INVALID.getErrorCode());
		Map<String, Object> fingerprint = new HashMap<>();
		fingerprint.put("errorCode", ISCDeviceTaskErrorEnum.DOWNLOAD_PERSON_NO_FINGERPRINT.getErrorCode());
		Map<String, Object> personDownloadDetail = new HashMap<>();
		personDownloadDetail.put("person", person);
		personDownloadDetail.put("cards", Collections.singletonList(card));
		personDownloadDetail.put("fingerprints", Collections.singletonList(fingerprint));
		Map<String, Object> detail = new HashMap<>();
		detail.put("personId", personId);
		detail.put("persondownloadResult",
				ISCDeviceTaskErrorEnum.DOWNLOAD_PERMISSION_PACKET_SEND_FAILED.getErrorCode());
		detail.put("personDownloadDetail", personDownloadDetail);
		Map<String, Object> body = new HashMap<>();
		body.put("total", 1);
		body.put("list", Collections.singletonList(detail));
		return JSONUtil.toJsonStr(body);
	}
}
