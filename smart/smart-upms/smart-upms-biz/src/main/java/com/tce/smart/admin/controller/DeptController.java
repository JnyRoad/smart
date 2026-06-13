package com.tce.smart.admin.controller;

import com.tce.smart.admin.api.entity.SysDept;
import com.tce.smart.admin.service.SysDeptService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * <p>
 * 部门管理 前端控制器
 * </p>
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dept")
@Api(value = "dept", description = "部门管理模块")
public class DeptController {
	private final SysDeptService sysDeptService;

	/**
	 * 通过ID查询
	 *
	 * @param id ID
	 * @return SysDept
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable Integer id) {
		return Result.success(sysDeptService.getById(id));
	}


	/**
	 * 返回树形菜单集合
	 *
	 * @return 树形菜单
	 */
	@GetMapping(value = "/tree")
	public Result getTree() {
		return Result.success(sysDeptService.selectTree());
	}

	/**
	 * 返回当前用户树形菜单集合
	 *
	 * @return 树形菜单
	 */
	@GetMapping(value = "/user-tree")
	public Result getUserTree() {
		return Result.success(sysDeptService.getUserTree());
	}

	/**
	 * 添加
	 *
	 * @param sysDept 实体
	 * @return success/false
	 */
	@SysLog("添加部门")
	@PostMapping("/save")
	@PreAuthorize("@pms.hasPermission('sys_dept_add')")
	public Result save(@Valid @RequestBody SysDept sysDept) {
		return Result.success(sysDeptService.saveDept(sysDept));
	}

	/**
	 * 删除
	 *
	 * @param id ID
	 * @return success/false
	 */
	@SysLog("删除部门")
	@PostMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('sys_dept_del')")
	public Result removeById(@PathVariable Integer id) {
		return Result.success(sysDeptService.removeDeptById(id));
	}

	/**
	 * 编辑
	 *
	 * @param sysDept 实体
	 * @return success/false
	 */
	@SysLog("编辑部门")
	@PostMapping("/update")
	@PreAuthorize("@pms.hasPermission('sys_dept_edit')")
	public Result update(@Valid @RequestBody SysDept sysDept) {
		sysDept.setUpdateTime(LocalDateTime.now());
		return Result.success(sysDeptService.updateDeptById(sysDept));
	}
}
