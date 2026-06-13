package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.ParkParkingRespDTO;
import com.tce.smart.platform.core.dto.AddSnapVehicleDTO;
import com.tce.smart.platform.core.dto.SnapVehicleAccessDTO;
import com.tce.smart.platform.core.dto.SnapVehicleDTO;
import com.tce.smart.platform.core.entity.SmtSnapVehicle;
import com.tce.smart.platform.core.vo.SearchOneSnapVehicleVO;
import com.tce.smart.platform.core.vo.SearchSmtSnapVehicleVO;
import com.tce.smart.platform.core.vo.SnapVehicleCountVO;

import java.util.List;

/**
 * 车辆抓拍记录表
 *
 * @author 王艳勇
 * @date 2019-04-13 18:18:20
 */
public interface SmtSnapVehicleService extends IService<SmtSnapVehicle> {

	/**
	 * 查询车辆统计信息
	 * @return
	 */
	SnapVehicleCountVO getVehicleCountBySnapTime(Integer parkId);

	/**
	 * 保存抓拍车辆信息
	 *
	 * @param entity 抓拍车辆信息
	 * @return 校验结果
	 */
	boolean saveSnapVehicle(AddSnapVehicleDTO entity);

//	boolean saveSnapVehicle(BridgeDTO<String> bridgeDTO);

	/**
	 * 查询车辆记录信息
	 *
	 * @param snapVehicleDTO 查询条件
	 * @return 返回车辆集合
	 */
	IPage getSnapVehicle(Page page,SnapVehicleDTO snapVehicleDTO);

	/**
	 * 查询车辆详情
	 * @param id 查询条件
	 * @return 返回车辆详情
	 */
	SmtSnapVehicle getSnapVehicleDetail(Long id);

	/**
	 * 查询车辆出入的信息
	 * @param page page
	 * @param snapVehicleAccessDto snapVehicleAccessDto
	 * @return
	 */
	 IPage<SearchSmtSnapVehicleVO> searchVehicleAccess(Page page, SnapVehicleAccessDTO snapVehicleAccessDto,String snapTime);


	 SearchOneSnapVehicleVO  searchVehicleAccessById(Integer id);

	/**
	 * 查询最近的车辆出入抓拍记录
	 * @param size
	 * @return
	 */
	List<ParkParkingRespDTO.InOutRecord> getInOutRecord(Integer parkId);
}
