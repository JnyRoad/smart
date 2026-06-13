package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.SaveXCVehicleDTO;
import com.tce.smart.platform.core.dto.UpdateXCVehicleDTO;
import com.tce.smart.platform.core.dto.XcVehicleDTO;
import com.tce.smart.platform.core.entity.SmtXcVehicle;

import java.util.List;

/**
 * 许昌车辆信息表
 *
 */
public interface SmtXcVehicleService extends IService<SmtXcVehicle> {


	IPage getXcVehicle(Page page, XcVehicleDTO entity);

	/**
	 * 保存许昌车辆绑定人员
	 *
	 * @param entity 车辆人员信息
	 * @return 返回保存结果
	 */
	Boolean saveXCSmtVehicle(SaveXCVehicleDTO entity);

	/**
	 * 修改车辆信息
	 * @return
	 */
	Boolean xcUpdateById(UpdateXCVehicleDTO updateXCVehicleDTO);

	/**
	 * 删除车辆信息
	 * @param id 车辆ID
	 * @return 返回结果
	 */
	Boolean deleteVehicle(Long id);
}
