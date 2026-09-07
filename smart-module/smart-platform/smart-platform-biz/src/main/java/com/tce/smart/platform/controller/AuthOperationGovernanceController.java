package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.dto.authgovernance.*;
import com.tce.smart.platform.service.impl.AuthOperationManagementActionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 权限操作治理入口；写接口仍由服务层再次读取当前安全上下文。 */
@RestController
@RequestMapping("/device/authority/operation")
public class AuthOperationGovernanceController extends BaseController {

	private final AuthOperationManagementActionService service;

	public AuthOperationGovernanceController(AuthOperationManagementActionService service) {
		this.service = service;
	}

	@GetMapping("/review/page")
	@PreAuthorize("@pms.hasPermission('platform_auth_operation_review_view')")
	public Result getParkReviews(AuthOperationReviewPageQuery query) {
		return success(service.getParkReviews(query));
	}

	@GetMapping("/review/global/page")
	@PreAuthorize("@pms.hasPermission('platform_auth_operation_global_review_view')")
	public Result getGlobalReviews(AuthOperationReviewPageQuery query) {
		return success(service.getGlobalReviews(query));
	}

	@PostMapping("/target/retry")
	@PreAuthorize("@pms.hasPermission('platform_auth_operation_retry')")
	@SysLog("权限治理重试完全未发送目标")
	public Result retry(@RequestBody AuthOperationRetryRequest request) {
		return success(service.retry(request));
	}

	@PostMapping("/target/{targetId}/manual-verification")
	@PreAuthorize("@pms.hasPermission('platform_auth_operation_manual_verify')")
	@SysLog("记录权限治理人工观察")
	public Result manualVerification(@PathVariable("targetId") Long targetId,
			@RequestBody AuthOperationManualVerificationRequest request) {
		return success(service.manualVerification(targetId, request));
	}

	@GetMapping("/target/{targetId}/actions")
	@PreAuthorize("@pms.hasPermission('platform_auth_operation_manual_verify') or @pms.hasPermission('platform_auth_operation_retry')")
	public Result getActions(@PathVariable("targetId") Long targetId, AuthOperationActionPageQuery query) {
		return success(service.getActions(targetId, query));
	}

	@GetMapping("/target/{targetId}/actions/{actionId}")
	@PreAuthorize("@pms.hasPermission('platform_auth_operation_manual_verify') or @pms.hasPermission('platform_auth_operation_retry')")
	public Result getAction(@PathVariable("targetId") Long targetId, @PathVariable("actionId") Long actionId) {
		return success(service.getAction(targetId, actionId));
	}
}
