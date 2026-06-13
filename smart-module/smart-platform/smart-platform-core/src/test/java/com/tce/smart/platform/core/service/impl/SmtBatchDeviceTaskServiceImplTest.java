package com.tce.smart.platform.core.service.impl;

import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.service.SmtBatchDeviceTaskService.VehicleInfo;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Collections;

public class SmtBatchDeviceTaskServiceImplTest {

	@Test
	public void createVehicleAuthTasksDoesNotCountUnsupportedIscVehicleResult() {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtBatchDeviceTaskServiceImpl service = new SmtBatchDeviceTaskServiceImpl(deviceTaskService,
				Mockito.mock(SmtDeviceService.class));
		Mockito.when(deviceTaskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("ISC车辆权限不支持下发");

		int taskCount = service.createVehicleAuthTasks(
				Collections.singletonList(new VehicleInfo("4001", "粤B12345")),
				Collections.emptyList(),
				Collections.singletonList("car-device-1"));

		Assert.assertEquals(0, taskCount);
		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(deviceTaskService).saveTask(taskCaptor.capture());
		Assert.assertEquals(DeviceTaskConstants.CAR, taskCaptor.getValue().getDeviceType());
		Assert.assertEquals(DeviceTaskActionEnum.DELAY_DOWN.getCode(), taskCaptor.getValue().getAction());
	}

	@Test
	public void createVehicleAuthTasksCountsNumericTaskIds() {
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtBatchDeviceTaskServiceImpl service = new SmtBatchDeviceTaskServiceImpl(deviceTaskService,
				Mockito.mock(SmtDeviceService.class));
		Mockito.when(deviceTaskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("10001");

		int taskCount = service.createVehicleAuthTasks(
				Collections.singletonList(new VehicleInfo("4001", "粤B12345")),
				Collections.singletonList("old-device"),
				Collections.singletonList("new-device"));

		Assert.assertEquals(2, taskCount);
		Mockito.verify(deviceTaskService, Mockito.times(2)).saveTask(Mockito.any(DeviceTaskVO.class));
	}
}
