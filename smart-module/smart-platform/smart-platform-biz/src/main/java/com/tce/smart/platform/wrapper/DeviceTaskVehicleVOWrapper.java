package com.tce.smart.platform.wrapper;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.vo.DeviceTaskVehicleVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 */
@Component
@AllArgsConstructor
public class DeviceTaskVehicleVOWrapper extends BaseWrapper<SmtDeviceTask, DeviceTaskVehicleVO> {

    @Override
    protected DeviceTaskVehicleVO warp(SmtDeviceTask SmtDeviceTask) throws IOException {
		DeviceTaskVehicleVO deviceTaskVehicle = new DeviceTaskVehicleVO();
		deviceTaskVehicle.setCardNo(SmtDeviceTask.getCardNo());
		deviceTaskVehicle.setPlate(SmtDeviceTask.getGeneral());
        return deviceTaskVehicle;
    }
}
