package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.securityzone.*;
import com.tce.smart.platform.api.dto.resp.securityzone.AuthApplyRemarkRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityStaffRespDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityZone;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:12:46
 */
public interface SmtSecurityZoneService extends IService<SmtSecurityZone> {

	/**
	 * 分页查询
	 * @param page
	 * @param query
	 * @return
	 */
	IPage<SmtSecurityZone> getPage(Page page, SecurityZoneQueryReqDTO query);

	/**
	 * 保存保密区
	 * @param edit
	 * @return
	 */
	Boolean saveZone(SecurityZoneEditReqDTO edit);

	/**
	 * 编辑保密区
	 * @param edit
	 * @return
	 */
	Boolean editZone(SecurityZoneEditReqDTO edit);

	/**
	 * 批量删除保密区
	 * @param query
	 * @return
	 */
	Boolean deleteZone(SecurityZoneQueryReqDTO query);


	/**
	 * 根据员工获得关联保密区
	 * @param staffId
	 * @return
	 */
	List<SmtSecurityZone> getSecurityZoneByStaff(Long staffId);


	/**
	 * 员工筛选
	 * @param reqDTO
	 * @return
	 */
	List<SecurityStaffRespDTO> getStaffByInfo(SecurityStaffQueryReqDTO reqDTO);


	/**
	 * 员工筛选中筛选
	 * @param reqDTO
	 * @return
	 */
	List<SecurityStaffRespDTO> getCheckStaff(SecurityStaffCheckReqDTO reqDTO);

	/**
	 * 员工申请权限时获取错误信息
	 * @param req
	 * @return
	 */
	List<AuthApplyRemarkRespDTO> getAuthRemark(List<AuthApplyRemarkReqDTO> req);

}
