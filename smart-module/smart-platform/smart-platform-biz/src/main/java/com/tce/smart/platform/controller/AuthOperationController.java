package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.dto.authoperation.AuthOperationBatchPageQuery;
import com.tce.smart.platform.dto.authoperation.AuthOperationTargetPageQuery;
import com.tce.smart.platform.service.impl.AuthOperationManagementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限操作管理查询入口。
 */
@RestController
@RequestMapping("/device/authority/operation")
public class AuthOperationController extends BaseController {

	private final AuthOperationManagementService service;

	public AuthOperationController(AuthOperationManagementService service) {
		this.service = service;
	}

	@GetMapping("/batch/page")
	public Result getBatchPage(AuthOperationBatchPageQuery query) {
		return success(service.getBatchPage(query, allowedParkIds()));
	}

	@GetMapping("/batch/{batchId}")
	public Result getBatch(@PathVariable("batchId") Long batchId) {
		return success(service.getBatch(batchId, allowedParkIds()));
	}

	@GetMapping("/target/page")
	public Result getTargetPage(AuthOperationTargetPageQuery query) {
		return success(service.getTargetPage(query, allowedParkIds()));
	}

	private java.util.List<Integer> allowedParkIds() {
		SmartUser user = SecurityUtils.getUser();
		return user == null ? null : user.getParkIdList();
	}
}
