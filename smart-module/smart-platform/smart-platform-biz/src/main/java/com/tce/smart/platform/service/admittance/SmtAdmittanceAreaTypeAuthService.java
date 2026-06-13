package com.tce.smart.platform.service.admittance;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.admittance.AdmittanceAuthEditReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAuthRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceAreaTypeAuth;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-08-17 17:45:23
 */
public interface SmtAdmittanceAreaTypeAuthService extends IService<SmtAdmittanceAreaTypeAuth> {

	/**
	 * 根据类型获得权限策略
	 * @param areaTypeId OA区域关联表ID
	 * @param authType 权限策略类型
	 * @return
	 */
	List<SmtAdmittanceAreaTypeAuth> getAuthByType(String areaTypeId, Integer authType, Integer parkId);

	/**
	 * 编辑区域类别关联权限
	 * @param reqDTO
	 * @return
	 */
	Boolean editAuth(List<AdmittanceAuthEditReqDTO> reqDTO);

	/**
	 * 获得权限关联列表
	 * @param parkId
	 * @return
	 */
	List<AdmittanceAuthRespDTO> getList(Integer parkId);

	/**
	 * 获得区域集合名
	 * @param parkId
	 * @param areaId
	 * @return
	 */
	String getAuthNameByAreaId(Integer parkId, String areaId);
}
