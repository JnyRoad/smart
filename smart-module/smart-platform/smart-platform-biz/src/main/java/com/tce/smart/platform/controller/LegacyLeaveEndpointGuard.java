package com.tce.smart.platform.controller;

import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 旧离职内部路由的临时兼容门。
 *
 * 默认拒绝；只有完成调用方盘点并显式配置专属 client 与用途后，才允许携带 actor/园区上下文的迁移请求。
 */
@Component
@RequiredArgsConstructor
public class LegacyLeaveEndpointGuard {
	private final OpenApiAuthenticationAdapter authenticationAdapter;

	@Value("${security.inner.leave-legacy.enabled:false}")
	private boolean enabled;

	@Value("${security.inner.leave-legacy.client-id:}")
	private String clientId;

	@Value("${security.inner.leave-legacy.purpose:}")
	private String purpose;

	public ActorScope assertCaller(String actorBadge, String actorParkIds, String from, String actualPurpose) {
		if (!enabled) {
			throw new AccessDeniedException("旧离职内部路由已停用");
		}
		Set<Integer> parks = parseParks(actorParkIds);
		Authentication authentication = SecurityUtils.getAuthentication();
		if (StrUtil.isBlank(actorBadge) || parks.isEmpty() || !SecurityConstants.FROM_IN.equals(from)
				|| StrUtil.isBlank(clientId) || StrUtil.isBlank(purpose) || !purpose.equals(actualPurpose)
				|| authentication == null || !authenticationAdapter.isClientOnly(authentication)
				|| !clientId.equals(authenticationAdapter.clientId(authentication))) {
			throw new AccessDeniedException("旧离职内部调用未获授权");
		}
		return new ActorScope(actorBadge, parks);
	}

	private Set<Integer> parseParks(String actorParkIds) {
		Set<Integer> parks = new HashSet<>();
		if (StrUtil.isBlank(actorParkIds)) {
			return parks;
		}
		for (String value : actorParkIds.split(",")) {
			try {
				parks.add(Integer.valueOf(value.trim()));
			} catch (RuntimeException ignored) {
				return new HashSet<>();
			}
		}
		return parks;
	}

	/** 已验证的迁移调用者上下文；浏览器和普通 server client 不能自行构造有效授权。 */
	public static final class ActorScope {
		private final String badge;
		private final Set<Integer> parkIds;

		private ActorScope(String badge, Set<Integer> parkIds) {
			this.badge = badge;
			this.parkIds = parkIds;
		}

		public String getBadge() {
			return badge;
		}

		public Set<Integer> getParkIds() {
			return parkIds;
		}
	}
}
