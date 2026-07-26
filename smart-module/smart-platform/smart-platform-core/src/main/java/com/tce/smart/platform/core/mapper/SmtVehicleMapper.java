package com.tce.smart.platform.core.mapper;

import java.util.List;

import com.tce.smart.platform.core.dto.VehicleAuthDTO;
import com.tce.smart.platform.core.dto.XcVehicleDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.vo.NotStaffVehicleVO;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.VehicleDTO;
import com.tce.smart.platform.core.entity.SmtVehicle;
import com.tce.smart.platform.core.model.VehicleStaff;
import com.tce.smart.platform.core.vo.VehicleCountVO;
import com.tce.smart.platform.core.vo.VehicleVO;

/**
 * 车辆信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:02
 */
public interface SmtVehicleMapper extends BaseMapper<SmtVehicle> {

	/**
	 * 查询车辆信息
	 * @param page 分页
	 * @param entity 查询条件
	 * @return 返回结果集
	 */
	IPage getVehicle(Page page, @Param("query") VehicleDTO entity);

	/**
	 * 查询员工车辆信息
	 * @param page 分页
	 * @param entity 查询条件
	 * @return 返回结果集
	 */
	IPage getStaffVehicle(Page page, @Param("query") VehicleDTO entity);


	/**
	 * 查询非员工车辆信息
	 * @param page 分页
	 * @param entity 查询条件
	 * @return 返回结果集
	 */
	IPage<NotStaffVehicleVO> getNotStaffVehicle(Page page, @Param("query") VehicleDTO entity);

	/**
	 * 查询车辆详情信息
	 * @param id 车辆ID
	 * @return 返回结果
	 */
	VehicleVO getDetail(Long id);

	/**
	 * 获取福利信息
	 * @return
	 */
	List<String> getWelfareLevel();

	/**
	 * 车辆获取园区的信息
	 * @param parkId
	 * @param vehicleId
	 * @param status
	 * @return
	 */
	int getApplyVehicle(@Param("parkId") Integer parkId, @Param("vehicleId") Long vehicleId,
			@Param("status") Integer status);

	/**
	 * 根据部门ID获取员工信息
	 * @param depId
	 * @return
	 */
	List<VehicleStaff> getStaff(@Param("depId") Integer depId);

	/**
	 * 删除车辆
	 * @param id
	 * @param status
	 * @return
	 */
	int deleteVehilce(@Param("id") Long id, @Param("status") Integer status);

	/**
	 * 车辆统计
	 * @return
	 */
	VehicleCountVO getVehicleCountInfo(@Param("parkIds") List<Integer> parkIds);

	List<SmtPark> getParkBu(@Param("compId") String compId, @Param("parkIds") List<Integer> parkIds);

	List<SmtPark> getParkTempBu(@Param("compId") String compId, @Param("parkIds") List<Integer> parkIds);

	/**
	 * 分页查询关联指定权限的指定所属车辆数据
	 * @param page
	 * @param authId
	 * @return
	 */
	IPage<VehicleAuthDTO> getVehicleAuth(Page page, @Param("authId")Integer authId, @Param("type")Integer type);

	/**
	 * 分页查询所有关联指定权限的车辆数据
	 * @param page
	 * @param authId
	 * @return
	 */
	IPage<VehicleAuthDTO> getVehicleAuthAll(Page page, @Param("authId")Integer authId);

}
