package com.tce.smart.admin.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.admin.api.entity.SysUserRole;

import java.util.List;

/**
 * <p>
 * 用户角色表 服务类
 * </p>
 *
 */
public interface SysUserRoleService extends IService<SysUserRole> {

	/**
	 * 根据用户Id删除该用户的角色关系
	 *
	 * @param userId 用户ID
	 * @return boolean
	 * @author 寻欢·李
	 * @date 2017年12月7日 16:31:38
	 */
	boolean deleteByUserId(Integer userId);

	/**
	 * 修改用户角色
	 * @param userId 用户ID
	 * @param roleIdList 角色ID集合
	 * @return boolean ture-成功，false-失败
	 */
	boolean updateUserRole(Integer userId,List<Integer> roleIdList);

	/**
	 * 批量修改用户角色
	 * @param userId 用户id
	 * @param roleIdList 角色ID集合
	 * @return boolean ture-成功，false-失败
	 */
	boolean saveUserRoleBatch(Integer userId, List<Integer> roleIdList);
}
