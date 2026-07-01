package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.DeviceAuthPersonReqDTO;
import com.tce.smart.platform.core.mapper.SmtDeviceAreaMapper;
import com.tce.smart.platform.core.mapper.SmtParkMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.service.SmtDevicePersonService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtDeviceVehicleService;
import com.tce.smart.platform.core.vo.DeviceVO;
import com.tce.smart.tool.enums.DeviceTypeEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SmtDeviceServiceImplTest {

	@Test
	public void clearAuthIsNoOpWhenDeviceHasNoAuthorizedPerson() {
		SmtParkMapper parkMapper = Mockito.mock(SmtParkMapper.class);
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtDevicePersonService devicePersonService = Mockito.mock(SmtDevicePersonService.class);
		SmtDeviceVehicleService deviceVehicleService = Mockito.mock(SmtDeviceVehicleService.class);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtDeviceAreaMapper deviceAreaMapper = Mockito.mock(SmtDeviceAreaMapper.class);

		SmtDeviceServiceImpl service = Mockito.spy(new SmtDeviceServiceImpl(parkMapper, deviceTaskService,
				devicePersonService, deviceVehicleService, staffMapper, deviceAreaMapper));

		DeviceVO deviceVO = new DeviceVO();
		deviceVO.setDeviceType(DeviceTypeEnum.DEVICE_TYPE_2.getCode());
		Mockito.doReturn(deviceVO).when(service).getDeviceById("device-A");
		Mockito.when(devicePersonService.getDeviceAuthPerson(Mockito.any(Page.class), Mockito.any(DeviceAuthPersonReqDTO.class)))
				.thenReturn(new Page<>());

		Boolean result = service.clearAuth("device-A");

		Assert.assertTrue(result);
		Mockito.verify(deviceTaskService, Mockito.never()).addDeviceDelTaskImmed(
				Mockito.anyList(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.any(), Mockito.anyInt(), Mockito.anyString());
	}
}
