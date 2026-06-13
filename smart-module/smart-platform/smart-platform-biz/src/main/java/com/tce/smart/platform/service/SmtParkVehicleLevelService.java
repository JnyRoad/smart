package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtParkVehicleLevel;

import java.util.List;

/**
 * 园区车辆入园职层表
 *
 * @author mckaywu
 * @date 2019-11-20 10:36:48
 */
public interface SmtParkVehicleLevelService extends IService<SmtParkVehicleLevel> {

	/**
	 * 根据园区ID查询关联关系
	 *
	 * @param parkId 园区ID
	 * @return 园区车辆入园申请职层关系列表
	 */
	List<SmtParkVehicleLevel> listByParkId(Integer parkId);

	/**
	 * 根据园区ID删除关联关系
	 *
	 * @param parkId 园区ID
	 * @return Boolean true-成功，fasle-失败
	 */
	Boolean removeByParkId(Integer parkId);

	/**
	 * 修改园区关联关系(先删除再新增)
	 *
	 * @param parkId   园区ID
	 * @param jcheList BU编号列表
	 * @return Boolean true-成功，fasle-失败
	 */
	Boolean saveParkVehicleLevel(Integer parkId, List<String> jcheList);

}
