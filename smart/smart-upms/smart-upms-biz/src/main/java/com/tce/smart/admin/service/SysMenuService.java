package com.tce.smart.admin.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.admin.api.entity.SysMenu;
import com.tce.smart.admin.api.vo.MenuVO;
import com.tce.smart.common.core.model.Result;

import java.util.List;

/**
 * <p>
 * 菜单权限表 服务类
 * </p>
 *
 */
public interface SysMenuService extends IService<SysMenu> {
	/**
	 * 通过角色编号查询URL 权限
	 *
	 * @param roleId 角色ID
	 * @return 菜单列表
	 */
	List<MenuVO> findMenuByRoleId(Integer roleId);

	/**
	 * 级联删除菜单
	 *
	 * @param id 菜单ID
	 * @return 成功、失败
	 */
	Result removeMenuById(Integer id);

	/**
	 * 更新菜单信息
	 *
	 * @param sysMenu 菜单信息
	 * @return 成功、失败
	 */
	Boolean updateMenuById(SysMenu sysMenu);
}
