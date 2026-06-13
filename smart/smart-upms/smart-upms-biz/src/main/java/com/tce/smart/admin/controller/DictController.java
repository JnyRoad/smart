package com.tce.smart.admin.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.service.SysDictService;
import com.tce.smart.common.core.constant.enums.ExceptionType;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 字典表 前端控制器
 * </p>
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dict")
@Api(value = "dict", description = "字典管理模块")
public class DictController extends BaseController {
	private final SysDictService sysDictService;

	/**
	 * 通过ID查询字典信息
	 *
	 * @param id ID
	 * @return 字典信息
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable Integer id) {
		return success(sysDictService.getById(id));
	}

	/**
	 * 分页查询字典信息
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/page")
	public Result<IPage> getDictPage(Page page, SysDict sysDict) {
		return success(sysDictService.page(page, Wrappers.<SysDict>lambdaQuery()
				.eq(StrUtil.isNotBlank(sysDict.getType()), SysDict::getType, sysDict.getType())));
	}


	/**
	 * 查询单条字典信息
	 *
	 * @param id
	 * @return 字典信息
	 */
	@Inner
	@GetMapping("/id")
	public Result findById(@RequestParam("id") Integer id) {
		SysDict dict = sysDictService.getById(id);
		return success(dict);
	}

	/**
	 * 查询单条字典信息
	 *
	 * @param type
	 * @param value
	 * @return 字典信息
	 */
	@Inner
	@GetMapping("/value")
	public Result findByValue(@RequestParam("type") String type, @RequestParam("value") String value) {
		SysDict dict = sysDictService.findByValue(type, value);
		return success(dict);
	}

	/**
	 * 通过字典类型查找字典
	 *
	 * @param type
	 * @return 字典信息
	 */
	@Inner
	@GetMapping("/type")
	public Result findByType(@RequestParam("type") String type) {
		List<SysDict> dictList = sysDictService.findByType(type);
		return success(dictList);
	}

	/**
	 * 通过字典类型查找字典
	 *
	 * @param type 类型
	 * @return 同类型字典
	 */
	@GetMapping("/type/{type}")
	public Result getDictByType(@PathVariable String type) {
		return Result.success(sysDictService.list(Wrappers
				.<SysDict>query().lambda()
				.eq(SysDict::getType, type)));
	}

	/**
	 * 保存字典信息
	 *
	 * @param type
	 * @return 字典信息
	 */
	@Inner
	@CacheEvict(value = "dict_details", key = "#type")
	@GetMapping("/save")
	public Result saveDict(@RequestParam("type") String type, @RequestParam("label") String label, @RequestParam("value") String value) {
		return success(sysDictService.saveDict(type, label, value));
	}

	/**
	 * 添加字典
	 *
	 * @param sysDict 字典信息
	 * @return success、false
	 */
	@SysLog("添加字典")
	@PostMapping("/add")
	@CacheEvict(value = "dict_details", key = "#sysDict.type")
	@PreAuthorize("@pms.hasPermission('sys_dict_add')")
	public Result save(@Valid @RequestBody SysDict sysDict) {
		return success(sysDictService.saveDict(sysDict));
	}

	/**
	 * 删除字典，并且清除字典缓存
	 *
	 * @param id   ID
	 * @param type 类型
	 * @return Result
	 */
	@SysLog("删除字典")
	@PostMapping("/{id}/{type}")
	@CacheEvict(value = "dict_details", key = "#type")
	@PreAuthorize("@pms.hasPermission('sys_dict_del')")
	public Result removeById(@PathVariable Integer id, @PathVariable String type) {
		return success(sysDictService.removeById(id));
	}

	/**
	 * 修改字典
	 *
	 * @param sysDict 字典信息
	 * @return success/false
	 */
	@PostMapping("/update")
	@SysLog("修改字典")
	@CacheEvict(value = "dict_details", key = "#sysDict.type")
	@PreAuthorize("@pms.hasPermission('sys_dict_edit')")
	public Result updateById(@Valid @RequestBody SysDict sysDict) {
		return Result.success(sysDictService.updateById(sysDict));
	}
}
