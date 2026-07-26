package com.tce.smart.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.admin.api.entity.SysSocialDetails;
import com.tce.smart.admin.api.dto.SocialDetailsSummaryRespDTO;
import com.tce.smart.admin.api.dto.SocialDetailsSecretRotateReqDTO;
import com.tce.smart.admin.api.dto.SocialDetailsUpdateReqDTO;
import com.tce.smart.admin.service.SysSocialDetailsService;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.Valid;
import java.util.stream.Collectors;


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
	@PreAuthorize("@pms.hasPermission('sys_client_edit')")
	public Result getSocialDetailsPage(Page page, SysSocialDetails sysSocialDetails) {
		Page<SysSocialDetails> resultPage = (Page<SysSocialDetails>) sysSocialDetailsService.page(page, Wrappers.query(sysSocialDetails));
		Page<SocialDetailsSummaryRespDTO> responsePage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
		responsePage.setRecords(resultPage.getRecords().stream().map(this::toSummary).collect(Collectors.toList()));
		return Result.success(responsePage);
	}


	/**
	 * 信息
	 *
	 * @param id
	 * @return Result
	 */
	@GetMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('sys_client_edit')")
	public Result getById(@PathVariable("id") Integer id) {
		return Result.success(toSummary(sysSocialDetailsService.getById(id)));
	}

	/**
	 * 保存
	 *
	 * @param sysSocialDetails
	 * @return Result
	 */
	@SysLog("保存三方信息")
	@PostMapping("/save")
	@PreAuthorize("@pms.hasPermission('sys_client_add')")
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
	@PreAuthorize("@pms.hasPermission('sys_client_edit')")
	public Result updateById(@Valid @RequestBody SocialDetailsUpdateReqDTO sysSocialDetails) {
		SysSocialDetails existing = requireExisting(sysSocialDetails.getId());
		existing.setType(sysSocialDetails.getType());
		existing.setRemark(sysSocialDetails.getRemark());
		existing.setAppId(sysSocialDetails.getAppId());
		existing.setRedirectUrl(sysSocialDetails.getRedirectUrl());
		sysSocialDetailsService.updateById(existing);
		return Result.success(Boolean.TRUE);
	}

	/**
	 * 显式轮换第三方 appSecret；普通编辑永远不接收该字段，避免前端脱敏响应把存量密钥覆盖为空。
	 */
	@SysLog("轮换三方账号密钥")
	@PutMapping("/secret/{id}")
	@PreAuthorize("@pms.hasPermission('sys_client_edit')")
	public Result rotateSecret(@PathVariable Integer id, @Valid @RequestBody SocialDetailsSecretRotateReqDTO request) {
		SysSocialDetails existing = requireExisting(id);
		existing.setAppSecret(request.getAppSecret());
		return Result.success(sysSocialDetailsService.updateById(existing));
	}

	/**
	 * 删除
	 *
	 * @param id
	 * @return Result
	 */
	@SysLog("删除三方信息")
	@PostMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('sys_client_del')")
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

	/** 管理端查询不得回传 OAuth appSecret。 */
	private SocialDetailsSummaryRespDTO toSummary(SysSocialDetails source) {
		if (source == null) {
			return null;
		}
		SocialDetailsSummaryRespDTO response = new SocialDetailsSummaryRespDTO();
		response.setId(source.getId());
		response.setType(source.getType());
		response.setRemark(source.getRemark());
		response.setAppId(source.getAppId());
		response.setRedirectUrl(source.getRedirectUrl());
		response.setCreateTime(source.getCreateTime());
		response.setUpdateTime(source.getUpdateTime());
		return response;
	}

	private SysSocialDetails requireExisting(Integer id) {
		SysSocialDetails existing = sysSocialDetailsService.getById(id);
		if (existing == null) {
			throw new TCEException("三方账号不存在");
		}
		return existing;
	}


}
