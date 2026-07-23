package com.tce.smart.platform.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.resp.InternalParkBridgeTargetRespDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.service.SmtParkService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 仅向内部受控服务公开的园区最小数据。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/park")
public class InternalParkController extends BaseController {

	private final SmtParkService smtParkService;
	private final OpenApiAuthenticationAdapter openApiAuthenticationAdapter;

	/** Dispatcher 服务客户端必须由受管配置精确指定，缺失时拒绝读取动态目标。 */
	@Value("${security.inner.park.dispatcher-client-id:}")
	private String dispatcherServiceClientId;

	/**
	 * 返回 Dispatcher 创建动态 Bridge 客户端必需的最小字段。
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/bridge-targets")
	public Result<List<InternalParkBridgeTargetRespDTO>> getBridgeTargets(
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from) {
		assertDispatcherCaller(from);
		List<SmtPark> parks = smtParkService.getUnStrainedParks();
		if (parks == null) {
			return success(Collections.emptyList());
		}
		List<InternalParkBridgeTargetRespDTO> targets = new ArrayList<>();
		for (SmtPark park : parks) {
			InternalParkBridgeTargetRespDTO target = new InternalParkBridgeTargetRespDTO();
			target.setId(park.getId());
			target.setBridgeUrl(park.getBridgeUrl());
			targets.add(target);
		}
		return success(targets);
	}

	/**
	 * 即使持有 server scope，也只能由运维受管的 Dispatcher client_id 读取动态目标。
	 */
	private void assertDispatcherCaller(String from) {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (!SecurityConstants.FROM_IN.equals(from) || dispatcherServiceClientId == null || dispatcherServiceClientId.trim().isEmpty()
				|| authentication == null || !openApiAuthenticationAdapter.isClientOnly(authentication)
				|| !dispatcherServiceClientId.equals(openApiAuthenticationAdapter.clientId(authentication))) {
			throw new AccessDeniedException("动态 Bridge 目标调用未获授权");
		}
	}
}
