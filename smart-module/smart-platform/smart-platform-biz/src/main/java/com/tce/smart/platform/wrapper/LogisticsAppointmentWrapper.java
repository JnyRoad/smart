package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtLogisticsAppointment;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtSnapVehicle;
import com.tce.smart.platform.core.vo.LogisticsAppointmentVO;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.service.SmtLogisticsAppointmentService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtSnapVehicleService;
import com.tce.smart.tool.constant.LogisticsAppointmentConstants;
import com.tce.smart.tool.enums.VehicleEventTypEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Slf4j
@Component
@AllArgsConstructor
public class LogisticsAppointmentWrapper extends BaseWrapper<SmtLogisticsAppointment, LogisticsAppointmentVO> {
	private final SmtLogisticsAppointmentService smtLogisticsAppointmentService;
	private final SmtSnapVehicleService smtSnapVehicleService;
	private final SmtParkService smtParkService;
	private final ImageService imageService;
    @Override
    protected LogisticsAppointmentVO warp(SmtLogisticsAppointment smtLogisticsAppointment) throws IOException {
		LogisticsAppointmentVO logisticsAppointmentVO = new LogisticsAppointmentVO();
		BeanUtil.copyProperties(smtLogisticsAppointment,logisticsAppointmentVO);
		if(ObjectUtil.isNotNull(smtLogisticsAppointment.getParkId())){
			SmtPark park = smtParkService.getById(smtLogisticsAppointment.getParkId());
			if(ObjectUtil.isNotNull(park)){
				logisticsAppointmentVO.setParkName(park.getParkName());
			}
		}

		List<SmtSnapVehicle> list = null;

		if(smtLogisticsAppointment.getStatus().equals(LogisticsAppointmentConstants.ARRIVED)){
			list = smtSnapVehicleService.list(Wrappers.<SmtSnapVehicle>query().lambda().eq(SmtSnapVehicle::getCardNo,smtLogisticsAppointment.getId()).eq(SmtSnapVehicle::getEventType,VehicleEventTypEnum.IN.getCode()).orderByDesc(SmtSnapVehicle::getCreateTime));
		}else if(smtLogisticsAppointment.getStatus().equals(LogisticsAppointmentConstants.ALREADY_LEFT)){
			list = smtSnapVehicleService.list(Wrappers.<SmtSnapVehicle>query().lambda().eq(SmtSnapVehicle::getCardNo,smtLogisticsAppointment.getId()).eq(SmtSnapVehicle::getEventType,VehicleEventTypEnum.OUT.getCode()).orderByDesc(SmtSnapVehicle::getCreateTime));
		}
		if(CollUtil.isNotEmpty(list)){
			SmtSnapVehicle smtSnapVehicle = list.get(0);
			logisticsAppointmentVO.setSnapPhotoId(imageService.buildImageUrl(smtSnapVehicle.getParkId(),smtSnapVehicle.getSnapPhotoId()));
			logisticsAppointmentVO.setAreaName(smtSnapVehicle.getAreaName());
			logisticsAppointmentVO.setSnapTime(smtSnapVehicle.getSnapTime());
			logisticsAppointmentVO.setEventTypeDesc(VehicleEventTypEnum.desc(smtSnapVehicle.getEventType()));
		}
        return logisticsAppointmentVO;
    }
}
