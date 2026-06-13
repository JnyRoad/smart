package com.tce.smart.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.admin.api.entity.SysRole;
import com.tce.smart.admin.service.SysRoleMenuService;
import com.tce.smart.admin.service.SysRoleService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.log.annotation.SysLog;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping("/role")
@Api(value = "role", description = "角色管理模块")
public class RoleController {
	private final SysRoleService sysRoleService;
	private final SysRoleMenuService sysRoleMenuService;

	/**
	 * 通过ID查询角色信息
	 *
	 * @param id ID
	 * @return 角色信息
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable Integer id) {
		return Result.success(sysRoleService.getById(id));
	}

	/**
	 * 添加角色
	 *
	 * @param sysRole 角色信息
	 * @return success、false
	 */
	@SysLog("添加角色")
	@PostMapping("/save")
	@PreAuthorize("@pms.hasPermission('sys_role_add')")
	public Result save(@Valid @RequestBody SysRole sysRole) {
		if(Objects.nonNull(sysRole)){
			sysRole.setCreateTime(DateUtils.localDateTime());
		}
		return Result.success(sysRoleService.save(sysRole));
	}

	/**
	 * 修改角色
	 *
	 * @param sysRole 角色信息
	 * @return success/false
	 */
	@SysLog("修改角色")
	@PostMapping("/update")
	@PreAuthorize("@pms.hasPermission('sys_role_edit')")
	public Result update(@Valid @RequestBody SysRole sysRole) {
		if(Objects.nonNull(sysRole)){
			sysRole.setUpdateTime(DateUtils.localDateTime());
		}
		return Result.success(sysRoleService.updateById(sysRole));
	}

	/**
	 * 删除角色
	 *
	 * @param id
	 * @return
	 */
	@SysLog("删除角色")
	@PostMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('sys_role_del')")
	public Result removeById(@PathVariable Integer id) {
		return Result.success(sysRoleService.removeRoleById(id));
	}

	/**
	 * 获取角色列表
	 *
	 * @return 角色列表
	 */
	@GetMapping("/list")
	public Result<List<SysRole>> listRoles() {
		return Result.success(sysRoleService.list(Wrappers.emptyWrapper()));
	}

	/**
	 * 分页查询角色信息
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/page")
	public Result getRolePage(Page page, SysRole sysRole) {
		return Result.success(sysRoleService.page(page, Wrappers.<SysRole>query().lambda()
				.like(StringUtils.isNotBlank(sysRole.getRoleName()), SysRole::getRoleName, sysRole.getRoleName())
				.like(StringUtils.isNotBlank(sysRole.getRoleCode()), SysRole::getRoleCode, sysRole.getRoleCode())
				.like(StringUtils.isNotBlank(sysRole.getRoleDesc()), SysRole::getRoleDesc, sysRole.getRoleDesc())
				.orderByDesc(SysRole::getCreateTime)));
	}

	/**
	 * 更新角色菜单
	 *
	 * @param roleId  角色ID
	 * @param menuIds 菜单ID拼成的字符串，每个id之间根据逗号分隔
	 * @return success、false
	 */
	@SysLog("更新角色菜单")
	@PostMapping("/menu")
	@PreAuthorize("@pms.hasPermission('sys_role_perm')")
	public Result saveRoleMenus(Integer roleId, @RequestParam(value = "menuIds", required = false) String menuIds) {
		SysRole sysRole = sysRoleService.getById(roleId);
		return Result.success(sysRoleMenuService.saveRoleMenus(sysRole.getRoleCode(), roleId, menuIds));
	}
}
