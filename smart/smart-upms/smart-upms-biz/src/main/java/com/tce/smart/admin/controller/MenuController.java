package com.tce.smart.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.admin.api.dto.MenuTree;
import com.tce.smart.admin.api.entity.SysMenu;
import com.tce.smart.admin.api.vo.MenuVO;
import com.tce.smart.admin.api.util.TreeUtil;
import com.tce.smart.admin.service.SysMenuService;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/menu")
@Api(value = "menu", description = "菜单管理模块")
public class MenuController {
	private final SysMenuService sysMenuService;

	/**
	 * 返回当前用户的树形菜单集合
	 *
	 * @return 当前用户的树形菜单
	 */
	@GetMapping
	public Result getUserMenu() {
		// 获取符合条件的菜单
		Set<MenuVO> all = new HashSet<>();
		SecurityUtils.getRoles()
				.forEach(roleId -> all.addAll(sysMenuService.findMenuByRoleId(roleId)));
		List<MenuTree> menuTreeList = all.stream()
				.filter(menuVo -> CommonConstants.MENU.equals(menuVo.getType()))
				.map(MenuTree::new)
				.sorted(Comparator.comparingInt(MenuTree::getSort))
				.collect(Collectors.toList());
		return Result.success(TreeUtil.build(menuTreeList, -1));
	}

	/**
	 * 返回树形菜单集合
	 *
	 * @return 树形菜单
	 */
	@GetMapping(value = "/tree")
	public Result getTree() {
		return Result.success(TreeUtil.buildTree(sysMenuService.list(Wrappers.emptyWrapper()), -1));
	}

	/**
	 * 返回角色的菜单集合
	 *
	 * @param roleId 角色ID
	 * @return 属性集合
	 */
	@GetMapping("/tree/{roleId}")
	public List getRoleTree(@PathVariable Integer roleId) {
		return sysMenuService.findMenuByRoleId(roleId)
				.stream()
				.map(MenuVO::getMenuId)
				.collect(Collectors.toList());
	}

	/**
	 * 通过ID查询菜单的详细信息
	 *
	 * @param id 菜单ID
	 * @return 菜单详细信息
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable Integer id) {
		return Result.success(sysMenuService.getById(id));
	}

	/**
	 * 新增菜单
	 *
	 * @param sysMenu 菜单信息
	 * @return success/false
	 */
	@SysLog("新增菜单")
	@PostMapping("/save")
	@PreAuthorize("@pms.hasPermission('sys_menu_add')")
	public Result save(@Valid @RequestBody SysMenu sysMenu) {
		return Result.success(sysMenuService.save(sysMenu));
	}

	/**
	 * 删除菜单
	 *
	 * @param id 菜单ID
	 * @return success/false
	 */
	@SysLog("删除菜单")
	@PostMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('sys_menu_del')")
	public Result removeById(@PathVariable Integer id) {
		return sysMenuService.removeMenuById(id);
	}

	/**
	 * 更新菜单
	 *
	 * @param sysMenu
	 * @return
	 */
	@SysLog("更新菜单")
	@PostMapping("/update")
	@PreAuthorize("@pms.hasPermission('sys_menu_edit')")
	public Result update(@Valid @RequestBody SysMenu sysMenu) {
		return Result.success(sysMenuService.updateMenuById(sysMenu));
	}

}
