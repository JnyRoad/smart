package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.entity.SmtVehicle;
import com.tce.smart.tool.enums.VehicleBelongTypeEnum;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SnapVehicleConstants;
import com.tce.smart.platform.core.dto.AddSnapVehicleDTO;
import com.tce.smart.platform.core.entity.SmtVehicleStaff;
import com.tce.smart.platform.core.vo.VehicleStaffVO;
import com.tce.smart.platform.core.mapper.SmtVehicleStaffMapper;
import com.tce.smart.platform.service.SmtNotStaffService;
import com.tce.smart.platform.service.SmtVehicleStaffService;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;

/**
 * 车辆员工关联表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:13
 */
@Service
@AllArgsConstructor
public class SmtVehicleStaffServiceImpl extends ServiceImpl<SmtVehicleStaffMapper, SmtVehicleStaff> implements SmtVehicleStaffService {

	private final SmtNotStaffService smtNotStaffService;

	/**
	 * 抓拍车辆如果是公司或员工车辆则补全车辆记录信息，否则不处理
	 * @param entity 抓拍车辆信息
	 * @return
	 */
	@Override
	public void vehicleStaffHandle(AddSnapVehicleDTO entity) {
		if(StrUtil.isNotBlank(entity.getCardNo())) {
			VehicleStaffVO vehicleStaffVO = this.baseMapper.getByVehicleID(entity.getCardNo());
			if(ObjectUtil.isNotNull(vehicleStaffVO)) {
				//员工车辆
				entity.setDriverId(vehicleStaffVO.getId());
				entity.setDriverName(vehicleStaffVO.getName());
				entity.setDriverPhone(vehicleStaffVO.getPhone());
				entity.setDriverType(VehicleBelongTypeEnum.STAFF_VEHICLE.getCode());
				entity.setVehicleAscription(VehicleBelongTypeEnum.STAFF_VEHICLE.getCode());
				entity.setVehicleBrand(vehicleStaffVO.getVehicleBrand());
				entity.setVehicleColor(vehicleStaffVO.getVehicleColor());
				entity.setVehicleType(vehicleStaffVO.getVehicleType());
			}else {
				vehicleStaffVO = smtNotStaffService.getByVehicleID(entity.getCardNo());
				if(ObjectUtil.isNotNull(vehicleStaffVO)) {
					//非员工车辆
					entity.setDriverId(vehicleStaffVO.getId());
					entity.setDriverName(vehicleStaffVO.getName());
					entity.setDriverPhone(vehicleStaffVO.getPhone());
					entity.setDriverType(VehicleBelongTypeEnum.NON_STAFF_VEHICLE.getCode());
					entity.setVehicleAscription(VehicleBelongTypeEnum.NON_STAFF_VEHICLE.getCode());
					entity.setVehicleBrand(vehicleStaffVO.getVehicleBrand());
					entity.setVehicleColor(vehicleStaffVO.getVehicleColor());
					entity.setVehicleType(vehicleStaffVO.getVehicleType());
				}
			}
		}
	}

	@Override
	public SmtVehicleStaff getByStaffId(Long staffId) {
		return this.baseMapper.selectOne(Wrappers.<SmtVehicleStaff>query().lambda().eq(SmtVehicleStaff::getStaffId, staffId));
	}

}
