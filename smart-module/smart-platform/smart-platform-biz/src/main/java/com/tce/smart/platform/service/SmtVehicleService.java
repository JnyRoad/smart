package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtVehicle;
import com.tce.smart.platform.core.model.DepTree;
import com.tce.smart.platform.core.model.VehicleDetail;
import com.tce.smart.platform.core.model.VehicleStaff;
import com.tce.smart.platform.core.vo.NotStaffVehicleVO;
import com.tce.smart.platform.core.vo.VehicleCountVO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsdepRespDTO;

import java.util.List;

/**
 * 车辆信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:02
 */
public interface SmtVehicleService extends IService<SmtVehicle> {

	/**
	 * 保存车辆绑定人员
	 *
	 * @param entity 车辆人员信息
	 * @return 返回保存结果
	 */
	Result saveSmtVehicle(SaveVehicleDTO entity);


	/**
	 * 保存车辆绑定人员-只添加车辆信息
	 *
	 * @param entity 车辆人员信息
	 * @return 返回保存结果
	 */
	Result saveSmtVehicleOnly(SaveVehicleDTO entity);

	/**
	 * 查询车辆信息
	 * @param page 分页
	 * @param entity 查询条件
	 * @return 返回结果集
	 */
	IPage getVehicle(Page page,VehicleDTO entity);

	/**
	 * 查询车辆详情信息
	 * @param id 车辆ID
	 * @return 返回结果
	 */
	VehicleDetail getVehicleDetail(Long id);


	/**
	 * 查询非员工车辆详情信息
	 * @param id 车辆ID
	 * @return 返回结果
	 */
	NotStaffVehicleVO getNotStaffVehicle(Long id);

	/**
	 * 获取BU信息
	 * @return 返回结果集
	 */
	List<OvwYscompRespDTO> getComp(List<Integer> parkIds);

	/**
     * 根据BU信息获取部门信息
     * @param id BUID
     * @return 返回结果集
     */
    List<OvwYsdepRespDTO> getDep(Integer id);

    /**
     * 根据部门ID获取员工信息
     * @param id 部门ID
     * @return 返回结果集
     */
    List<VehicleStaff> getStaff(Integer id);

	/**
	 * 删除车辆详情信息
	 * @param id 车辆ID
	 * @param  parkIds 园区ID
	 * @return 返回结果
	 */
	boolean deleteVehicle(Long id, List<Integer> parkIds);

	/**
	 * 更新车辆信息
	 * @param entity
	 * @return
	 */
	Result<Boolean> updateVehicle(VehicleDTO entity);

	/**
	 * 根据ID获取员工详情
	 * @param staffId
	 * @return
	 */
	SmtStaff getStaffDetail(Long staffId);

	/**
	 * 根据员工号获取详情
	 * @param badge
	 * @return
	 */
	VehicleStaff getStaffDetail(String badge,List<Integer> parkIds);

	/**
	 * 获取bu部门级联数组
	 * @return
	 */
	List<DepTree> getCompTree(List<Integer> parkIds);

	/**
	 * 车辆统计
	 * @return
	 */
	VehicleCountVO getVehicleCountInfo();

	/**
	 * 查询非员工车辆信息
	 * @param page 分页
	 * @param smtVehicle 查询条件
	 * @return 返回结果集
	 */
	IPage<NotStaffVehicleVO> getNotStaffVehiclePage(Page page, VehicleDTO smtVehicle);

	/**
	 * 非员工车辆信息保存
	 * @param notStaffVehicleDTO 车辆信息
	 * @return 返回结果集
	 */
	Result saveNotStaffVehicle(NotStaffVehicleDTO notStaffVehicleDTO);

	/**
	 * 获取福利信息
	 * @return
	 */
	List<String> getWelfareLevel();

	/**
	 * 车辆获取园区的信息
	 * @param parkId
	 * @param vehiclePlate
	 * @param isDelete
	 * @param status
	 * @return
	 */
	int getApplyVehicle(Integer parkId,String vehiclePlate,Integer isDelete,Integer status);

	/**
	 * 非员工车辆更新
	 * @param notStaffVehicleDTO
	 * @return
	 */
	boolean updateNotStaffVehicle(NotStaffVehicleDTO notStaffVehicleDTO);

	/**
	 * 非员工车辆删除
	 * @param id
	 * @return
	 */
	boolean deleteNotStaffVehicle(Long id);

	/**
	 * 字符串分离为id
	 * @param idsStr
	 * @return
	 */
	List<Integer> splitStringToInteger(String idsStr);

}
