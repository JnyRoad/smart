package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.securityzone.OaAreaRelationEditReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.OaAreaRelationRespDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtOaAreaRelation;
import org.omg.CORBA.INTERNAL;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:44
 */
public interface SmtOaAreaRelationService extends IService<SmtOaAreaRelation> {

	/**
	 * 获得OA区域与权限关联列表
	 * @param parkId
	 * @return
	 */
	List<OaAreaRelationRespDTO> getList(Integer parkId);

	/**
	 * 编辑权限关联
	 * @param req
	 * @return
	 */
	Boolean editRelation(List<OaAreaRelationEditReqDTO> req);

	/**
	 * 根据区域id获得权限名
	 * @param areaId
	 * @return
	 */
	String getAuthNameByAreaId(Integer parkId, List<Integer> areaId);

	/**
	 * 权限关联列表查询
	 * @param parkId
	 * @param typeId
	 * @return
	 */
	List<SmtOaAreaRelation> getListByAreaId(Integer parkId, List<Integer> typeId);
}
