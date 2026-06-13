package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.AddAuthRelationReqDTO;
import com.tce.smart.platform.core.entity.SmtBusinessDeviceAuth;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2020-08-05 18:22:56
 */
public interface SmtBusinessDeviceAuthService extends IService<SmtBusinessDeviceAuth> {

	/**
	 * 获得权限关联表id
	 * @param parkId
	 * @param businessCode
	 * @return
	 */
	SmtBusinessDeviceAuth getDeviceAuth(Integer parkId, Integer businessCode);

	/**
	 * 新增关联
	 * @param smtBusinessDeviceAuth
	 * @return
	 */
	Boolean saveAuth(SmtBusinessDeviceAuth smtBusinessDeviceAuth);

	/**
	 * 保存部门与权限关系
	 * @param deptId
	 * @param authIdArray
	 * @return
	 */
	Boolean saveDeptAuth(String deptId, Integer[] authIdArray);

	/**
	 * 新增关联
	 * @param addAuthRelationReqDTO
	 * @return
	 */
	Boolean batchSaveAuth(AddAuthRelationReqDTO addAuthRelationReqDTO);

	/**
	 * 获取权限列表
	 * @param parkId
	 * @return
	 */
	AddAuthRelationReqDTO getList(Integer parkId);

	/**
	 * 多园区获得权限关联
	 * @param parkIds
	 * @param businessCode
	 * @return
	 */
	List<SmtBusinessDeviceAuth> getMulDeviceAuth(List<Integer> parkIds, Integer businessCode);

}
