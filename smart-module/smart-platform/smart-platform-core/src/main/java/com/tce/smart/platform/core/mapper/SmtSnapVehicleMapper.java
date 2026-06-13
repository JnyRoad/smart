package com.tce.smart.platform.core.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SnapVehicleAccessDTO;
import com.tce.smart.platform.core.dto.SnapVehicleCountDTO;
import com.tce.smart.platform.core.dto.SnapVehicleDTO;
import com.tce.smart.platform.core.entity.SmtSnapVehicle;
import com.tce.smart.platform.core.vo.SearchOneSnapVehicleVO;
import com.tce.smart.platform.core.vo.SearchSmtSnapVehicleVO;

/**
 * 车辆抓拍记录表
 *
 * @author 王艳勇
 * @date 2019-04-13 18:18:20
 */
public interface SmtSnapVehicleMapper extends BaseMapper<SmtSnapVehicle> {

	/**
	 * 查询车辆记录信息
	 * @param page 分页对象
	 * @param snapVehicleDto 查询条件
	 * @return 返回车辆集合
	 */
	IPage getSnapVehicle(Page page, @Param("query") SnapVehicleDTO snapVehicleDTO);

	/**
	 * 查询当前停车场车辆信息
	 * @param page 分页对象
	 * @param snapVehicleDto 查询条件
	 * @return 返回车辆集合
	 */
	IPage getCurrentVehicle(Page page, @Param("query") SnapVehicleDTO snapVehicleDTO);

	/**
	 * 查询集合外部人员的车辆出入信息
	 * @param page
	 * @param snapVehicleAccessDto
	 * @return
	 */
	IPage<SearchSmtSnapVehicleVO> searchVehicleVisitorAccess(Page page,@Param("query") SnapVehicleAccessDTO snapVehicleAccessDto,@Param("park") List<Integer> parkIdList);

	/**
	 * 查询入厂申请车辆
	 * @param page
	 * @param snapVehicleAccessDto
	 * @param parkIdList
	 * @return
	 */
	IPage<SearchSmtSnapVehicleVO> searchVehicleAdmittanceAccess(Page page,@Param("query") SnapVehicleAccessDTO snapVehicleAccessDto,@Param("park") List<Integer> parkIdList);

	/**
	 * 查询集合内部员工的车辆出入信息
	 * @param page
	 * @param snapVehicleAccessDto
	 * @return
	 */
	IPage<SearchSmtSnapVehicleVO> searchVehicleStaffAccess(Page page,@Param("query") SnapVehicleAccessDTO snapVehicleAccessDto, @Param("park") List<Integer> parkIdList);

	/**
	 * 查询外部人员的车辆出入信息
	 * @param id
	 * @return
	 */
	SearchOneSnapVehicleVO searchVehicleAccessVisitorById(@Param("query") SmtSnapVehicle smtSnapVehicle);

	/**
	 * 查询外部人员的车辆出入信息
	 * @param id
	 * @return
	 */
	SearchOneSnapVehicleVO searchVehicleAccessAdmittanceById(@Param("query") SmtSnapVehicle smtSnapVehicle);

	/**
	 * 查询内部人员的车辆出入信息
	 * @param id
	 * @return
	 */
	SearchOneSnapVehicleVO searchVehicleAccessStaffById(@Param("query") SmtSnapVehicle smtSnapVehicle);

	/**
	 * 查询当天的车辆抓拍信息
	 * @param page
	 * @param snapVehicleAccessDto
	 * @return
	 */
	List<SmtSnapVehicle> getSnapVehicleList(@Param("query") SnapVehicleAccessDTO snapVehicleAccessDto);

	/**
	 * 查询车辆统计信息
	 * @return
	 */
	List<SnapVehicleCountDTO> getVehicleCountBySnapTime(@Param("parkId") Integer parkId);

	SearchOneSnapVehicleVO searchVehicleAccessById(@Param("query") SmtSnapVehicle smtSnapVehicle);


	/**
	 * 查询最近的车辆抓拍信息
	 * @param size
	 * @return
	 */
	List<SmtSnapVehicle> getSnapVehicleListLasted(@Param("parkId") Integer parkId);

}
