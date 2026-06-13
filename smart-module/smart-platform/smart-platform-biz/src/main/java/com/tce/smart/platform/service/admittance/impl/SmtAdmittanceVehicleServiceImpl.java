package com.tce.smart.platform.service.admittance.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.platform.api.dto.req.admittance.AdmittanceVehicleReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceVehicleRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceVehicle;
import com.tce.smart.platform.core.mapper.SmtAdmittanceVehicleMapper;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceVehicleService;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.RegexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 入厂申请预约车辆表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:05
 */
@Service
public class SmtAdmittanceVehicleServiceImpl extends ServiceImpl<SmtAdmittanceVehicleMapper, SmtAdmittanceVehicle> implements SmtAdmittanceVehicleService {

	@Autowired
	private ImageService imageService;

	@Override
	public Boolean saveVehicle(List<AdmittanceVehicleReqDTO> reqDTOs, Long applyId) {
		if (CollUtil.isEmpty(reqDTOs)) {
			return Boolean.FALSE;
		}
		reqDTOs.forEach(reqDTO -> {
			if (!RegexUtils.matchVehicle(reqDTO.getPlate())) {
				throw new TCEException(ExceptionTypeEnum.VISITOR_VEHICLE_PLATE_ERROR);
			}
			SmtAdmittanceVehicle vehicle = BeanUtils.transform(SmtAdmittanceVehicle.class, reqDTO);
			vehicle.setVisitorId(applyId);
			this.save(vehicle);
		});
		return Boolean.TRUE;
	}

	@Override
	public List<SmtAdmittanceVehicle> getByApplyId(Long applyId) {
		return this.list(Wrappers.<SmtAdmittanceVehicle>query().lambda().eq(SmtAdmittanceVehicle::getVisitorId, applyId));
	}

	@Override
	public List<AdmittanceVehicleRespDTO> getRespByApplyId(Long applyId) {
		List<SmtAdmittanceVehicle> vehicleList = this.getByApplyId(applyId);
		List<AdmittanceVehicleRespDTO> vehicleRespDTOS = new ArrayList<>();
		vehicleList.forEach(vehicle -> {
			AdmittanceVehicleRespDTO vehicleRespDTO = BeanUtils.transform(AdmittanceVehicleRespDTO.class, vehicle);
			vehicleRespDTO.setCertTypeDesc(AdmittanceVehicleCertTypeEnum.desc(vehicle.getCertType()));
			vehicleRespDTO.setColourDesc(VehicleColorEnum.desc(vehicle.getColour()));
			vehicleRespDTO.setVehicleTypeDesc(AdmittanceVehicleTypeEnum.desc(vehicle.getVehicleType()));
			if(StrUtil.isNotEmpty(vehicle.getCertImg())) {
				vehicleRespDTO.setCertImgUrl(imageService.buildImageUrl(vehicle.getCertImg()));
			}
			vehicleRespDTOS.add(vehicleRespDTO);
		});
		return vehicleRespDTOS;
	}
}
