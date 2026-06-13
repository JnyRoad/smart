package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthRelationReqDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthRelation;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:12:53
 */
public interface SmtSecurityAuthRelationService extends IService<SmtSecurityAuthRelation> {

	/**
	 * 编辑保密区权限
	 * @param dto
	 * @return
	 */
	Boolean editAuth(List<SmtSecurityAuthRelation> dto);

	/**
	 * 获得保密区权限列表
	 * @param securityZoneId
	 * @return
	 */
	List<SmtSecurityAuthRelation> getList(Long securityZoneId);

	/**
	 * 批量获得保密区权限列表
	 * @param securityZoneId
	 * @return
	 */
	List<SmtSecurityAuthRelation> getBatchList(List<Long> securityZoneId);

	/**
	 * 删除权限
	 * @param securityZoneId
	 * @return
	 */
	Boolean deleteAuth(Long securityZoneId);

	/**
	 * 批量删除权限
	 * @param securityZoneIds
	 * @return
	 */
	Boolean batchDeleteAuth(List<Long> securityZoneIds);
}
