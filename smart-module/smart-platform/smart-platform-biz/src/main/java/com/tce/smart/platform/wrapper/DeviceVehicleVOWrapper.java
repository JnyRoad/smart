package com.tce.smart.platform.wrapper;

import cn.hutool.core.date.DateUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.dto.DeviceVehicleDTO;
import com.tce.smart.platform.core.vo.DeviceVehicleVO;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 */
@Component
@AllArgsConstructor
public class DeviceVehicleVOWrapper extends BaseWrapper<DeviceVehicleDTO, DeviceVehicleVO> {

    @Override
    protected DeviceVehicleVO warp(DeviceVehicleDTO deviceVehicleDTO) throws IOException {
		DeviceVehicleVO deviceVehicle = new DeviceVehicleVO();
		deviceVehicle.setCardNo(deviceVehicleDTO.getCardNo());
		deviceVehicle.setName(deviceVehicleDTO.getPersonName());
		deviceVehicle.setCreateTime(DateUtil.format(deviceVehicleDTO.getCreateTime(),"yyyy-MM-dd HH:mm"));
		deviceVehicle.setPlate(deviceVehicleDTO.getPlate());
		deviceVehicle.setStatus((deviceVehicleDTO.getOverTime().getTime() / 1000)<= DateUtil.currentSeconds() ? DeviceTaskConstants.DELING : DeviceTaskConstants.NORMAL);
        return deviceVehicle;
    }
}
