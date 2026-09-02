package com.tce.smart.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.admin.api.entity.SysOauthClientDetails;
import com.tce.smart.admin.service.SysOauthClientDetailsService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.openapi.OpenApiScope;
import com.tce.smart.common.security.openapi.OpenApiScopeCatalog;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@AllArgsConstructor
@RequestMapping("/client")
@Api(value = "client", description = "客户端管理模块")
public class OauthClientDetailsController {
	private final SysOauthClientDetailsService sysOauthClientDetailsService;

	/**
	 * 通过ID查询
	 *
	 * @param id ID
	 * @return SysOauthClientDetails
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable Integer id) {
		return Result.success(sysOauthClientDetailsService.getById(id));
	}


	/**
	 * 简单分页查询
	 *
	 * @param page                  分页对象
	 * @param sysOauthClientDetails 系统终端
	 * @return
	 */
	@GetMapping("/page")
	public Result getOauthClientDetailsPage(Page page, SysOauthClientDetails sysOauthClientDetails) {
		return Result.success(sysOauthClientDetailsService.page(page, Wrappers.query(sysOauthClientDetails)));
	}

	/**
	 * 返回受版本控制的 capability scope 目录，供客户端管理页的多选授权域使用。
	 *
	 * <p>该接口只暴露名称、展示标签与废弃标记；真正的创建/编辑权限仍由既有 save/update
	 * 接口控制，且服务层会再次校验，不能仅依赖前端下拉。</p>
	 *
	 * @return 可选择或展示的开放 API scope 目录
	 */
	@GetMapping("/scopes")
	public Result<List<OpenApiScope>> scopes() {
		return Result.success(OpenApiScopeCatalog.all());
	}

	/**
	 * 添加
	 *
	 * @param sysOauthClientDetails 实体
	 * @return success/false
	 */
	@SysLog("添加终端")
	@PostMapping("/save")
	@PreAuthorize("@pms.hasPermission('sys_client_add')")
	public Result add(@Valid @RequestBody SysOauthClientDetails sysOauthClientDetails) {
		return Result.success(sysOauthClientDetailsService.save(sysOauthClientDetails));
	}

	/**
	 * 删除
	 *
	 * @param id ID
	 * @return success/false
	 */
	@SysLog("删除终端")
	@PostMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('sys_client_del')")
	public Result removeById(@PathVariable String id) {
		return Result.success(sysOauthClientDetailsService.removeClientDetailsById(id));
	}

	/**
	 * 编辑
	 *
	 * @param sysOauthClientDetails 实体
	 * @return success/false
	 */
	@SysLog("编辑终端")
	@PostMapping("/update")
	@PreAuthorize("@pms.hasPermission('sys_client_edit')")
	public Result update(@Valid @RequestBody SysOauthClientDetails sysOauthClientDetails) {
		return Result.success(sysOauthClientDetailsService.updateClientDetailsById(sysOauthClientDetails));
	}

	/**
	 * 重置指定客户端的 secret：生成新的 32 位随机明文，编码后落库，并吊销该客户端下的旧 token。
	 *
	 * <p>安全注意：新明文 secret 只在本次响应里返回一次，SysLog 记录的是操作动作而非明文本身，
	 * 调用方拿到后必须自行妥善保存，服务端不会再次展示。</p>
	 *
	 * @param clientId 客户端ID
	 * @return 新生成的明文 secret
	 */
	@SysLog("重置终端密钥")
	@PutMapping("/secret/{clientId}")
	@PreAuthorize("@pms.hasPermission('sys_client_edit')")
	public Result<String> resetSecret(@PathVariable String clientId) {
		return Result.success(sysOauthClientDetailsService.resetSecret(clientId));
	}
}
