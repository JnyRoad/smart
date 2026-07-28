package com.tce.smart.platform.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.LogisticsAppointmentReqDTO;
import com.tce.smart.platform.core.dto.LogisticsAppointmentDTO;
import com.tce.smart.platform.service.SmtLogisticsAppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/** 定时同步物流预约专用入口，不影响管理端原保存接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/logistics/appointment")
public class InternalLogisticsAppointmentController extends BaseController {
	private final SmtLogisticsAppointmentService service;
	private final OpenApiAuthenticationAdapter authenticationAdapter;
	@Value("${security.inner.logistics.schedule-client-id:}")
	private String scheduleClientId;

	@Inner
	@OpenApi("server")
	@PostMapping("/save")
	public Result<Boolean> save(@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose,
			@Valid @RequestBody LogisticsAppointmentReqDTO request) {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (!SecurityConstants.FROM_IN.equals(from) || !"logistics-sync".equals(purpose)
				|| scheduleClientId == null || scheduleClientId.trim().isEmpty() || authentication == null
				|| !authenticationAdapter.isClientOnly(authentication)
				|| !scheduleClientId.equals(authenticationAdapter.clientId(authentication))) {
			throw new AccessDeniedException("物流预约同步调用未获授权");
		}
		LogisticsAppointmentDTO appointment = new LogisticsAppointmentDTO();
		BeanUtils.copyProperties(request, appointment);
		return success(service.saveLogisticsAppointment(appointment));
	}
}
