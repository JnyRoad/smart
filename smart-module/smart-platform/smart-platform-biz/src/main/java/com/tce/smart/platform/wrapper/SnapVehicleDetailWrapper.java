package com.tce.smart.platform.wrapper;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtSnapVehicle;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.vo.SnapVehicleDetailVO;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.tool.enums.VehicleColorEnum;
import com.tce.smart.tool.enums.VehicleEventTypEnum;
import com.tce.smart.tool.enums.VehicleTypeEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class SnapVehicleDetailWrapper extends BaseWrapper<SmtSnapVehicle, SnapVehicleDetailVO> {
//	private final SmtVehicleStaffMapper smtVehicleStaffMapper;
	private final ImageService imageService;
	private final SmtParkService smtParkService;
	private final SmtStaffService smtStaffService;
    @Override
    protected SnapVehicleDetailVO warp(SmtSnapVehicle snapVehicle) throws IOException {
	SmtStaff smtStaff = smtStaffService.getById(snapVehicle.getDriverId());
	SnapVehicleDetailVO snapVehicleDetailVO = new SnapVehicleDetailVO();
	snapVehicleDetailVO.setCompName(smtStaff.getCompName());
	snapVehicleDetailVO.setDepName(smtStaff.getDepName());
	snapVehicleDetailVO.setDriverName(snapVehicle.getDriverName());
	snapVehicleDetailVO.setDriverPhone(snapVehicle.getDriverPhone());
	snapVehicleDetailVO.setEventType(VehicleEventTypEnum.desc(snapVehicle.getEventType()));
	snapVehicleDetailVO.setId(snapVehicle.getId());
	if(ObjectUtil.isNotNull(snapVehicle.getSnapPhotoId())) {
		snapVehicleDetailVO.setSnapPhotoId(imageService.buildImageUrl(snapVehicle.getSnapPhotoId()));
	}
	snapVehicleDetailVO.setSnapTime(DateUtil.format(snapVehicle.getSnapTime(),"yyyy-MM-dd HH:mm"));
//    	VehicleStaffVO vehicleStaffVO = smtVehicleStaffMapper.getByVehiclePlate(snapVehicle.getVehiclePlate());
//    	if(ObjectUtil.isNotNull(vehicleStaffVO)) {
//    		snapVehicleDetailVO.setVehicleType(VehicleTypeEnum.desc(vehicleStaffVO.getVehicleType()));
//    	}
	snapVehicleDetailVO.setVehicleType(VehicleTypeEnum.OTHER.getDesc());
	snapVehicleDetailVO.setVehicleBrand(snapVehicle.getVehicleBrand());
	snapVehicleDetailVO.setVehicleColor(VehicleColorEnum.desc(snapVehicle.getVehicleColor()));
	snapVehicleDetailVO.setVehiclePlate(snapVehicle.getVehiclePlate());
	if(ObjectUtil.isNotNull(snapVehicle.getParkId()))
	{
		SmtPark byId = smtParkService.getById(snapVehicle.getParkId());
		snapVehicleDetailVO.setParkName(byId.getParkName());
	}
        return snapVehicleDetailVO;
    }
}
