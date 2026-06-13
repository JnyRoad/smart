package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.AddAuthRelationReqDTO;
import com.tce.smart.platform.core.entity.SmtBusinessDeviceAuth;
import com.tce.smart.platform.service.SmtBusinessDeviceAuthService;
import com.tce.smart.platform.service.securityzone.SmtSecurityPersonRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import java.util.Objects;

/**
 * @author fushiping
 * @date 2020-08-05 18:22:56
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-权限设置")
@RequestMapping("/business/auth")
public class SmtBusinessDeviceAuthController extends BaseController {

	private final SmtBusinessDeviceAuthService smtBusinessDeviceAuthService;

	private final SmtSecurityPersonRelationService smtSecurityPersonRelationService;

	/**
	 * 分页查询
	 *
	 * @param page                  分页对象
	 * @param smtBusinessDeviceAuth
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtBusinessDeviceAuthPage(Page page, SmtBusinessDeviceAuth smtBusinessDeviceAuth) {
		return success(smtBusinessDeviceAuthService.page(page, Wrappers.query(smtBusinessDeviceAuth)));
	}

	/**
	 * 获得园区-bu-部门-员工树形结构
	 *
	 * @return
	 */
	@ApiOperation("获得bu-部门树结构")
	@GetMapping("/bu/dept/tree/{parkId}")
	public Result getBuDeptTree(@PathVariable("parkId") Integer parkId) {
		return success(smtSecurityPersonRelationService.getComp(parkId, true));
	}

	@PostMapping("/dept/save/{deptId}")
	@ApiOperation(value = "保存部门与权限关系")
	public Result<Boolean> saveDeptAuth(@PathVariable("deptId") String deptId,
										@RequestParam(value = "authIdArray[]", required = false) Integer[] authIdArray) {
		return success(smtBusinessDeviceAuthService.saveDeptAuth(deptId, authIdArray));
	}

	/**
	 * @param
	 * @param smtBusinessDeviceAuth
	 * @return
	 */
	@GetMapping("/id")
	public Result getAuthId(SmtBusinessDeviceAuth smtBusinessDeviceAuth) {
		if (Objects.isNull(smtBusinessDeviceAuth.getParkId())) {
			return null;
		}
		SmtBusinessDeviceAuth auth = smtBusinessDeviceAuthService.getDeviceAuth(smtBusinessDeviceAuth.getParkId(),
				smtBusinessDeviceAuth.getBusinessCode());
		if (Objects.nonNull(auth)) {
			return success(auth.getAuthId());
		}
		return null;
	}

	/**
	 * @param
	 * @param parkId
	 * @return
	 */
	@GetMapping("/list")
	public Result getList(@RequestParam("parkId") Integer parkId) {
		return success(smtBusinessDeviceAuthService.getList(parkId));
	}


	/**
	 * 通过id查询
	 *
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		return success(smtBusinessDeviceAuthService.getById(id));
	}

	/**
	 * 新增
	 *
	 * @param smtBusinessDeviceAuth
	 * @return Result
	 */
	@SysLog("新增或编辑")
	@PostMapping("/edit")
	public Result save(@RequestBody SmtBusinessDeviceAuth smtBusinessDeviceAuth) {
		return success(smtBusinessDeviceAuthService.saveAuth(smtBusinessDeviceAuth));
	}

	/**
	 * 批量新增或编辑
	 *
	 * @param addAuthRelationReqDTO
	 * @return Result
	 */
	@SysLog("批量新增或编辑")
	@PostMapping("/batch/edit")
	public Result batchSave(@RequestBody AddAuthRelationReqDTO addAuthRelationReqDTO) {
		return success(smtBusinessDeviceAuthService.batchSaveAuth(addAuthRelationReqDTO));
	}


}
