package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityPersonAddReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityPersonExcelAddReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityPersonQueryReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityStaffQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityPersonAddRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.StaffTreeRespDTO;
import com.tce.smart.platform.core.dto.SecurityAllStaffListDTO;
import com.tce.smart.platform.core.dto.SecurityPersonRelationDTO;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityPersonRelation;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:00
 */
public interface SmtSecurityPersonRelationService extends IService<SmtSecurityPersonRelation> {

	/**
	 * 获取分页数据
	 * @param page
	 * @param reqDTO
	 * @return
	 */
	IPage<SecurityPersonRelationDTO> getPage(Page page,SecurityPersonQueryReqDTO reqDTO);

	/**
	 * 手动添加员工新增关联
	 * @param reqDTO
	 * @return
	 */
	Boolean saveRelation(List<SecurityPersonAddReqDTO> reqDTO);

	/**
	 * 导入员工新增关联
	 * @param reqDTO
	 * @return
	 */
	String saveExportRelation(List<SecurityPersonExcelAddReqDTO> reqDTO);

	/**
	 * 获得员工树形结构
	 * @return
	 */
	List<StaffTreeRespDTO> getStaffTree();

	/**
	 * 查询BU
	 * @param parkId
	 * @param isDept
	 * @return
	 */
	List<StaffTreeRespDTO> getComp(Integer parkId, boolean isDept);

	/**
	 * 根据部门id获得员工列表
	 * @param depId
	 * @return
	 */
	List<SecurityPersonAddRespDTO> getStaffByDepId(String depId);

	/**
	 * 批量删除
	 * @param reqDTO
	 * @return
	 */
	Boolean batchDelete(SecurityPersonQueryReqDTO reqDTO);

	/**
	 * 根据保密区id批量删除
	 * @param securityZoneIds
	 * @return
	 */
	Boolean batchDeleteByZoneId(List<Long> securityZoneIds);

	/**
	 * 获取所有在职员工
	 * @param page
	 * @param reqDTO
	 * @return
	 */
	IPage<SecurityAllStaffListDTO> getAllStaffPage(Page page, SecurityPersonQueryReqDTO reqDTO);

	/**
	 * 查询某个员工是否存在
	 * @param staffId
	 * @return
	 */
	List<SmtSecurityPersonRelation> getByStaffId(Long staffId);
}
