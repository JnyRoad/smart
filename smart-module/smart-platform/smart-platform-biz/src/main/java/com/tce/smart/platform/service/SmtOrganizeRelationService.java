package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.OrganizeRelationReqDTO;
import com.tce.smart.platform.core.entity.SmtOrganizeRelation;

import java.util.List;

/**
 *
 *
 * @author
 * @date 2019-04-15 11:34:43
 */
public interface SmtOrganizeRelationService extends IService<SmtOrganizeRelation> {

	/**
	 * 新增自定义bu
	 *
	 * @param relationReqDTO
	 * @return
	 */
	Boolean saveBu(OrganizeRelationReqDTO relationReqDTO);

	/**
	 * 修改自定义bu
	 *
	 * @param relationReqDTO
	 * @return
	 */
	Boolean updateBu(OrganizeRelationReqDTO relationReqDTO);

	Boolean deleteBu(Long id);

	/**
	 * 同步bu
	 *
	 * @param buList
	 * @return
	 */
	Boolean saveSyncBu(List<String> buList, Integer parkId);

	/**
	 * 根据userId获得bu信息
	 *
	 * @param userId
	 * @return
	 */
	SmtOrganizeRelation getByUserId(Integer userId);

	List<SmtOrganizeRelation> getByParkId(List<Integer> parkIds);
	/**
	 * 根据userName获得bu信息
	 *
	 * @param userName
	 * @return
	 */
	SmtOrganizeRelation getByUserName(String userName);

	SmtOrganizeRelation getByBu(Long buId);

	/**
	 * 是否为排除的临时BU
	 * @param compId
	 * @return
	 */
	Boolean wasExcludeOrg(Long compId);
	/**
	 * 查询需排除的临时BUId列表
	 * @param
	 * @return
	 */
	List<Long> wasExcludeOrgIds();

}
