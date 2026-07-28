package com.tce.smart.platform.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.resp.InternalParkBridgeTargetRespDTO;
import com.tce.smart.platform.api.dto.SmtParkDTO;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	/** 全园区内部列表只允许经配置批准的服务读取；空配置必须拒绝。 */
	@Value("${security.inner.park.list-client-ids:}")
	private String parkListClientIds;

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
	 * 面向受管服务的全园区列表，替代同时供 Smart UI 使用的 /park/app/all。
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/all")
	public Result<List<SmtParkDTO>> getAllParks(
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		if (!"park-list".equals(purpose)) {
			throw new AccessDeniedException("园区列表调用用途未获授权");
		}
		assertAllowedCaller(from, allowedClients(parkListClientIds), "园区列表调用未获授权");
		return success(smtParkService.getUnStrainedParks(), SmtParkDTO.class);
	}

	/**
	 * 即使持有 server scope，也只能由运维受管的 Dispatcher client_id 读取动态目标。
	 */
	private void assertDispatcherCaller(String from) {
		assertAllowedCaller(from, Collections.singleton(dispatcherServiceClientId), "动态 Bridge 目标调用未获授权");
	}

	/** server scope 不等于任意服务可读；必须同时命中配置的精确 client_id 白名单。 */
	private void assertAllowedCaller(String from, Set<String> allowedClientIds, String message) {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (!SecurityConstants.FROM_IN.equals(from) || allowedClientIds.isEmpty()
				|| authentication == null || !openApiAuthenticationAdapter.isClientOnly(authentication)
				|| !allowedClientIds.contains(openApiAuthenticationAdapter.clientId(authentication))) {
			throw new AccessDeniedException(message);
		}
	}

	private Set<String> allowedClients(String configuredClientIds) {
		if (configuredClientIds == null || configuredClientIds.trim().isEmpty()) {
			return Collections.emptySet();
		}
		Set<String> clientIds = new HashSet<>();
		for (String clientId : Arrays.asList(configuredClientIds.split(","))) {
			if (clientId != null && !clientId.trim().isEmpty()) {
				clientIds.add(clientId.trim());
			}
		}
		return clientIds;
	}
}
