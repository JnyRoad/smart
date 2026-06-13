package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.AddSnapVehicleDTO;
import com.tce.smart.platform.core.entity.SmtVehicleStaff;

/**
 * 车辆员工关联表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:13
 */
public interface SmtVehicleStaffService extends IService<SmtVehicleStaff> {

	/**
	 * 抓拍车辆如果是公司或员工车辆则补全车辆记录信息，否则不处理
	 * @param entity 抓拍车辆信息
	 * @return
	 */
	void vehicleStaffHandle(AddSnapVehicleDTO entity);

	SmtVehicleStaff getByStaffId(Long staffId);
}
