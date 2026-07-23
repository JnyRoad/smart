package com.tce.smart.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.admin.api.entity.SysSocialDetails;
import com.tce.smart.admin.service.SysSocialDetailsService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * 系统社交登录账号表
 *
 */
@RestController
@RequestMapping("/social")
@AllArgsConstructor
@Api(value = "social", description = "三方账号管理模块")
public class SocialDetailsController {
	private final SysSocialDetailsService sysSocialDetailsService;


	/**
	 * 社交登录账户简单分页查询
	 *
	 * @param page             分页对象
	 * @param sysSocialDetails 社交登录
	 * @return
	 */
	@GetMapping("/page")
	public Result getSocialDetailsPage(Page page, SysSocialDetails sysSocialDetails) {
		return Result.success(sysSocialDetailsService.page(page, Wrappers.query(sysSocialDetails)));
	}


	/**
	 * 信息
	 *
	 * @param id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		return Result.success(sysSocialDetailsService.getById(id));
	}

	/**
	 * 保存
	 *
	 * @param sysSocialDetails
	 * @return Result
	 */
	@SysLog("保存三方信息")
	@PostMapping("/save")
	public Result save(@Valid @RequestBody SysSocialDetails sysSocialDetails) {
		return Result.success(sysSocialDetailsService.save(sysSocialDetails));
	}

	/**
	 * 修改
	 *
	 * @param sysSocialDetails
	 * @return Result
	 */
	@SysLog("修改三方信息")
	@PostMapping("/update")
	public Result updateById(@Valid @RequestBody SysSocialDetails sysSocialDetails) {
		sysSocialDetailsService.updateById(sysSocialDetails);
		return Result.success(Boolean.TRUE);
	}

	/**
	 * 删除
	 *
	 * @param id
	 * @return Result
	 */
	@SysLog("删除三方信息")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable Integer id) {
		return Result.success(sysSocialDetailsService.removeById(id));
	}

	/**
	 * 绑定社交账号
	 *
	 * @param state 类型
	 * @param code  code
	 * @return
	 */
	@PostMapping("/bind")
	public Result bindSocial(String state, String code) {
		return Result.success(sysSocialDetailsService.bindSocial(state, code));
	}


}
