package com.tce.smart.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.admin.api.entity.SysRole;
import com.tce.smart.common.core.model.Result;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 */
public interface SysRoleService extends IService<SysRole> {

	/**
	 * 通过用户ID，查询角色信息
	 *
	 * @param userId
	 * @return
	 */
	List<SysRole> findRolesByUserId(Integer userId);

	/**
	 * 通过角色ID，删除角色
	 *
	 * @param id
	 * @return
	 */
	Result<Boolean> removeRoleById(Integer id);
}
