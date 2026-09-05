package com.tce.smart.platform.controller.securityzone;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthDeleteLogPageQueryReqDTO;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthDeleteLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 保密区权限自动删除记录报表接口。
 *
 * <p>所有查询都由报表服务按当前登录令牌的园区范围执行，导出权限单独控制。</p>
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-保密区权限自动删除记录")
@RequestMapping("/security/auth/delete/log")
public class SmtSecurityAuthDeleteLogController extends BaseController {

	private final SmtSecurityAuthDeleteLogService service;

	/**
	 * 分页查询自动删除审计记录。
	 *
	 * @param current 当前页，从1开始，默认1
	 * @param size 页大小，默认20，最大100
	 * @param query 组合筛选条件
	 * @return 分页结果
	 */
	@PreAuthorize("@pms.hasPermission('platform_security_auth_delete_log_view')")
	@GetMapping("/page")
	@ApiOperation("自动删除记录分页")
	public Result page(@RequestParam(value = "current", defaultValue = "1") long current,
			@RequestParam(value = "size", defaultValue = "20") long size,
			SecurityAuthDeleteLogPageQueryReqDTO query) {
		return success(service.page(new Page<>(current, size), query));
	}

	/**
	 * 导出当前筛选范围的自动删除审计 CSV。
	 *
	 * @param query 组合筛选条件
	 * @param response 下载响应
	 */
	@PreAuthorize("@pms.hasPermission('platform_security_auth_delete_log_export')")
	@GetMapping("/export")
	@ApiOperation("自动删除记录导出")
	public void export(SecurityAuthDeleteLogPageQueryReqDTO query, HttpServletResponse response) {
		service.export(query, response);
	}

	/**
	 * 查询一条自动删除审计记录的全部设备任务。
	 *
	 * @param id 审计记录主键
	 * @return 任务明细
	 */
	@PreAuthorize("@pms.hasPermission('platform_security_auth_delete_log_view')")
	@GetMapping("/{id}/tasks")
	@ApiOperation("自动删除记录任务明细")
	public Result tasks(@PathVariable("id") String id) {
		return success(service.tasks(id));
	}
}
