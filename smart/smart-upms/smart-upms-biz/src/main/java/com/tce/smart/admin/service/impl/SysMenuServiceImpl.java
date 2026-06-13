package com.tce.smart.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysMenu;
import com.tce.smart.admin.api.entity.SysRoleMenu;
import com.tce.smart.admin.api.vo.MenuVO;
import com.tce.smart.admin.mapper.SysMenuMapper;
import com.tce.smart.admin.mapper.SysRoleMenuMapper;
import com.tce.smart.admin.service.SysMenuService;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.model.Result;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 菜单权限表 服务实现类
 * </p>
 */
@Service
@AllArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
	private final SysRoleMenuMapper sysRoleMenuMapper;

	@Override
	@Cacheable(value = "menu_details", key = "#roleId  + '_menu'")
	public List<MenuVO> findMenuByRoleId(Integer roleId) {
		return baseMapper.listMenusByRoleId(roleId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value = "menu_details", allEntries = true)
	public Result removeMenuById(Integer id) {
		// 查询父节点为当前节点的节点
		List<SysMenu> menuList = this.list(Wrappers.<SysMenu>query()
				.lambda().eq(SysMenu::getParentId, id));
		if (CollUtil.isNotEmpty(menuList)) {
			return Result.builder()
					.code(CommonConstants.FAIL)
					.msg("菜单含有下级不能删除").build();
		}

		sysRoleMenuMapper
				.delete(Wrappers.<SysRoleMenu>query()
						.lambda().eq(SysRoleMenu::getMenuId, id));

		//删除当前菜单及其子菜单
		return new Result(this.removeById(id));
	}

	@Override
	@CacheEvict(value = "menu_details", allEntries = true)
	public Boolean updateMenuById(SysMenu sysMenu) {
		sysMenu.setUpdateTime(LocalDateTime.now());
		return this.updateById(sysMenu);
	}
}
