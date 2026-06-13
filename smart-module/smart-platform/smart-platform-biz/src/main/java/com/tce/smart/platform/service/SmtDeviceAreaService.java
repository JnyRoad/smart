package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.AddSnapVehicleDTO;
import com.tce.smart.platform.core.entity.SmtAlarmRecord;
import com.tce.smart.platform.core.entity.SmtDeviceArea;
import com.tce.smart.platform.core.entity.SmtSnapVehicle;

/**
 * 设备区域关联
 *
 * @author 王艳勇
 * @date 2019-04-15 15:12:58
 */
public interface SmtDeviceAreaService extends IService<SmtDeviceArea> {

	/**
	 * 车辆抓拍记录区域信息补充
	 *
	 * @param entity 抓拍车辆信息
	 */
	void areaHandle(AddSnapVehicleDTO entity);

	/**
	 * 警报记录区域信息补充
	 *
	 * @param entity 抓拍车辆信息
	 */
	Integer areaHandle(SmtAlarmRecord entity);

	/**
	 * 保存设备区域信息
	 *
	 * @param entity 抓拍车辆信息
	 * @return 结果
	 */
	Boolean saveArea(SmtDeviceArea entity);

}
