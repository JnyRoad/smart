package com.tce.smart.platform.controller.manage;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.service.manage.SmtStaffRechargeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 仅供 smart-schedule 执行充值同步的入口，避免封死管理页仍使用的原路由。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/recharge")
public class InternalStaffRechargeController extends BaseController {

	private final SmtStaffRechargeService smtStaffRechargeService;
	private final OpenApiAuthenticationAdapter openApiAuthenticationAdapter;

	@Value("${security.inner.recharge.schedule-client-id:}")
	private String scheduleClientId;

	@Inner
	@OpenApi("server")
	@GetMapping("/new")
	public Result<Boolean> syncNew(@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		assertScheduleCaller(from, purpose);
		return success(smtStaffRechargeService.syncNewStaff());
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/senior")
	public Result<Boolean> syncSenior(@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		assertScheduleCaller(from, purpose);
		return success(smtStaffRechargeService.syncSeniorRecharge());
	}

	private void assertScheduleCaller(String from, String purpose) {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (!SecurityConstants.FROM_IN.equals(from) || !"recharge-task".equals(purpose)
				|| scheduleClientId == null || scheduleClientId.trim().isEmpty()
				|| authentication == null || !openApiAuthenticationAdapter.isClientOnly(authentication)
				|| !scheduleClientId.equals(openApiAuthenticationAdapter.clientId(authentication))) {
			throw new AccessDeniedException("充值定时任务调用未获授权");
		}
	}
}
