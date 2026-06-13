package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityPersonQueryReqDTO;
import com.tce.smart.platform.core.dto.SearchStaffDTO;
import com.tce.smart.platform.core.dto.SearchToStaffDTO;
import com.tce.smart.platform.core.dto.SecurityAllStaffListDTO;
import com.tce.smart.platform.core.dto.StaffBadgeDTO;
import com.tce.smart.platform.core.entity.SmtAppAuth;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtVehicle;
import com.tce.smart.platform.core.entity.ext.SecurityPersonRelationExt;
import com.tce.smart.platform.core.vo.*;
import org.apache.ibatis.annotations.Param;

import javax.xml.crypto.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


/**
 * 员工表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:42
 */
public interface SmtStaffMapper extends BaseMapper<SmtStaff> {

	MyDormitoryVO myDormitory(SmtStaff smtStaff);

	SmtStaff SelectLastStaff(StaffBadgeDTO badgeDto);

	IPage<StaffListVO> getSmtStaffPage(@Param("page") Page page, @Param("query") SearchStaffDTO smtStaff, @Param("park") List<Integer> parkIdList);

	IPage<SecurityAllStaffListDTO> getStaffPage(Page page, @Param("query") SecurityPersonRelationExt reqDTO);

	List<VehicleApplyVO> getVehiclePark( @Param("vehiclePlate") Long vehiclePlate);

	VehicleParkDetailVO getVehicleParkById(Integer id);

	IPage<SmtVehicle> getMyVehicleByBadge(Page page, @Param("badge") String badge);

	Boolean updatePhone(SmtStaff smtStaff);

	Boolean updateReportTo(Long staffId);

	IPage<StaffNODormitoryVO> quetyStaffNODormitory(Page page,@Param("query")  SearchStaffDTO smtStaff,@Param("park") List<Integer> parkIdList);

	List<SmtAppAuth> getStaffAppAuth(long staffId);

	List<StaffDeviceAuthListVO> getStaffDeviceStafff(Integer authId);

	Page<StaffListVO> getTOStaffPage(Page page, @Param("query") SearchToStaffDTO searchToStaffDTO);

	/**
	 * 获取当月入职员工
	 * @return
	 */
	List<SmtStaff> getNewStaff();

	/**
	 * 获取往月入职员工
	 * @return
	 */
	List<SmtStaff> getSeniorStaff();

	/**
	 * 获取往月入职员工
	 * @return
	 */
	List<SmtStaff> getSeniorRechargeStaff();

	/**
	 * 根据员工编号模糊查询
	 * @return
	 */
	List<SmtStaff> getStaffLikeBadge(@Param("badge") String badge);

	List<StaffListVO> remoteSyncStaffInfo(@Param("parkId") Integer parkId, @Param("createTime") String createTime);

	Boolean updateStaffLeaveStatus(@Param("staffId") Long staffId, @Param("leaType") String leaType, @Param("leaDate") Date leaDate);

	SmtStaff getStaffIgnoreCase(@Param("badge") String badge);

	List<SmtStaff> listIscCardImportStaff(@Param("parkId") Integer parkId, @Param("badge") String badge,
										  @Param("staffScope") String staffScope);
}
