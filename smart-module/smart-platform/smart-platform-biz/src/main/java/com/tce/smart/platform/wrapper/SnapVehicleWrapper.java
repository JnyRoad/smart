package com.tce.smart.platform.wrapper;

import java.io.IOException;

import cn.hutool.core.util.ObjectUtil;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.service.SmtParkService;
import org.springframework.stereotype.Component;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtSnapVehicle;
import com.tce.smart.platform.core.vo.SnapVehicleVO;
import com.tce.smart.tool.enums.VehicleEventTypEnum;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class SnapVehicleWrapper extends BaseWrapper<SmtSnapVehicle, SnapVehicleVO> {
	private final SmtParkService smtParkService;
    @Override
    protected SnapVehicleVO warp(SmtSnapVehicle snapVehicle) throws IOException {
	SnapVehicleVO snapVehicleVO = new SnapVehicleVO();
	snapVehicleVO.setDriverName(snapVehicle.getDriverName());
	snapVehicleVO.setDriverPhone(snapVehicle.getDriverPhone());
	snapVehicleVO.setEventType(VehicleEventTypEnum.desc(snapVehicle.getEventType()));
	snapVehicleVO.setId(snapVehicle.getId());
	snapVehicleVO.setSnapTime(DateUtil.format(snapVehicle.getSnapTime(),"yyyy-MM-dd HH:mm"));
	snapVehicleVO.setVehiclePlate(snapVehicle.getVehiclePlate());
		SmtPark park = smtParkService.getById(snapVehicle.getParkId());
		if(ObjectUtil.isNotNull(park)){
			snapVehicleVO.setParkName(park.getParkName());
		}
        return snapVehicleVO;
    }
}
