package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.VehicleApplyDTO;
import com.tce.smart.platform.core.entity.SmtVehicleApply;
import com.tce.smart.platform.core.vo.VehicleApplyDetailVO;

import java.util.List;

/**
 * 入园申请信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:02
 */
public interface SmtVehicleApplyService extends IService<SmtVehicleApply> {

	/**
	 * 查询入园申请信息
	 * @param page 分页
	 * @param entity 查询条件
	 * @return 返回结果集
	 */
	IPage getVehicleApply(Page page,VehicleApplyDTO entity);

	/**
	 * 查询入园申请详情信息
	 * @param id 车辆ID
	 * @return 返回结果
	 */
	VehicleApplyDetailVO getVehicleApplyDetail(Long id);

	/**
	 * 更新入园申请信息
	 * @param entity
	 * @return
	 */
	boolean updateStatus(SmtVehicleApply entity);

	Boolean removeAuthToDevice(List<Integer> ids, List<String> deviceIds);

	Boolean removeAuthToDevice(SmtVehicleApply vehicleApply, List<String> deviceIds);

	Boolean removeByAuthId(Integer authId);
}
