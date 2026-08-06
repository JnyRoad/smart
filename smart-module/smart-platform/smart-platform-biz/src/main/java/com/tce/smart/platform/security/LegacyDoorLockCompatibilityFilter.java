package com.tce.smart.platform.security;

import com.tce.smart.common.core.security.LegacyCompatibilityCidr;
import com.tce.smart.common.core.security.LegacyCompatibilityProof;
import com.tce.smart.common.data.security.RedisNonceReplayGuard;
import com.tce.smart.platform.conf.LegacyDoorLockCompatibilityProperties;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 保护 OAuth 白名单中的三个同路径门锁兼容入口。
 *
 * <p>即使请求已通过 Gateway，Platform 仍二次验证签名、时间窗口和来源 CIDR，
 * 并使用 Redis 原子消费 nonce。这样绕过 Gateway 的直连流量无法借助 ignore-urls
 * 访问兼容控制器。</p>
 */
public class LegacyDoorLockCompatibilityFilter extends OncePerRequestFilter {

	private static final Set<String> COMPATIBILITY_PATHS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			"/dormitory/staff/remote/to/lock",
			"/park/tolock/dormitory/allList",
			"/staff/define/badge")));

	private final LegacyDoorLockCompatibilityProperties properties;
	private final RedisNonceReplayGuard replayGuard;

	public LegacyDoorLockCompatibilityFilter(LegacyDoorLockCompatibilityProperties properties,
			RedisNonceReplayGuard replayGuard) {
		this.properties = properties;
		this.replayGuard = replayGuard;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !isCompatibilityPathCandidate(request.getRequestURI());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (!"GET".equals(request.getMethod()) || !COMPATIBILITY_PATHS.contains(request.getRequestURI())) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		LegacyCompatibilityProof.Claims claims = readClaims(request);
		LegacyDoorLockCompatibilityProperties.Client client = findConfiguredClient(claims);
		long nowEpochSeconds = Instant.now().getEpochSecond();
		if (claims == null || client == null || !isValidProof(claims, request, nowEpochSeconds)
				|| !matchesClientSource(client, claims.getSourceIp())) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		// Redis TTL 覆盖 Platform 对 Gateway 时钟偏差的容忍窗口，不能在 grace period 先过期。
		long ttlSeconds = claims.getExpiresAtEpochSeconds() + properties.getMaxClockSkewSeconds() - nowEpochSeconds;
		RedisNonceReplayGuard.ReserveResult reserveResult = replayGuard.reserve(claims.getKeyId(), claims.getCallerId(),
				claims.getNonce(), Math.max(1L, ttlSeconds));
		if (reserveResult == RedisNonceReplayGuard.ReserveResult.UNAVAILABLE) {
			response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			return;
		}
		if (reserveResult != RedisNonceReplayGuard.ReserveResult.ACCEPTED) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		request.setAttribute(LegacyDoorLockCallerContext.REQUEST_ATTRIBUTE,
				new LegacyDoorLockCallerContext(client.getId(), client.getParkIds()));
		filterChain.doFilter(request, response);
	}

	private LegacyCompatibilityProof.Claims readClaims(HttpServletRequest request) {
		try {
			String keyId = request.getHeader(LegacyCompatibilityProof.HEADER_KEY_ID);
			String callerId = request.getHeader(LegacyCompatibilityProof.HEADER_CALLER_ID);
			String sourceIp = request.getHeader(LegacyCompatibilityProof.HEADER_SOURCE_IP);
			long issuedAt = Long.parseLong(request.getHeader(LegacyCompatibilityProof.HEADER_ISSUED_AT));
			long expiresAt = Long.parseLong(request.getHeader(LegacyCompatibilityProof.HEADER_EXPIRES_AT));
			String nonce = request.getHeader(LegacyCompatibilityProof.HEADER_NONCE);
			return LegacyCompatibilityProof.Claims.of(LegacyCompatibilityProof.VERSION, keyId, callerId, sourceIp,
					request.getMethod(), request.getRequestURI(), request.getQueryString(), issuedAt, expiresAt, nonce);
		} catch (RuntimeException exception) {
			return null;
		}
	}

	private LegacyDoorLockCompatibilityProperties.Client findConfiguredClient(LegacyCompatibilityProof.Claims claims) {
		if (claims == null || !isConfigurationValid()) {
			return null;
		}
		for (LegacyDoorLockCompatibilityProperties.Client client : properties.getClients()) {
			if (client != null && claims.getCallerId().equals(client.getId()) && client.getParkIds() != null
					&& !client.getParkIds().isEmpty()) {
				return client;
			}
		}
		return null;
	}

	private boolean isValidProof(LegacyCompatibilityProof.Claims claims, HttpServletRequest request, long nowEpochSeconds) {
		if (!LegacyCompatibilityProof.VERSION.equals(claims.getVersion()) || !properties.getKeyId().equals(claims.getKeyId())
				|| !request.getRequestURI().equals(claims.getServicePath()) || !request.getMethod().equals(claims.getMethod())) {
			return false;
		}
		return LegacyCompatibilityProof.verify(claims, request.getHeader(LegacyCompatibilityProof.HEADER_SIGNATURE),
				properties.getSignatureKey(), nowEpochSeconds, properties.getMaxClockSkewSeconds(),
				properties.getMaxTtlSeconds());
	}

	/** 配置整体不完整或含歧义调用方时拒绝全部兼容流量，避免部分条目被意外放行。 */
	private boolean isConfigurationValid() {
		if (!properties.isEnabled() || isBlank(properties.getKeyId()) || isBlank(properties.getSignatureKey())
				|| properties.getMaxClockSkewSeconds() < 0 || properties.getMaxTtlSeconds() <= 0
				|| properties.getClients() == null || properties.getClients().isEmpty()) {
			return false;
		}
		Set<String> callerIds = new HashSet<>();
		for (LegacyDoorLockCompatibilityProperties.Client client : properties.getClients()) {
			if (client == null || isBlank(client.getId()) || !callerIds.add(client.getId())
					|| !hasUniquePositiveParkIds(client.getParkIds()) || !hasValidUniqueCidrs(client.getSourceCidrs())) {
				return false;
			}
		}
		return true;
	}

	private boolean hasUniquePositiveParkIds(List<Integer> parkIds) {
		if (parkIds == null || parkIds.isEmpty()) {
			return false;
		}
		Set<Integer> uniqueParkIds = new HashSet<>();
		for (Integer parkId : parkIds) {
			if (parkId == null || parkId <= 0 || !uniqueParkIds.add(parkId)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasValidUniqueCidrs(List<String> sourceCidrs) {
		if (sourceCidrs == null || sourceCidrs.isEmpty()) {
			return false;
		}
		Set<String> uniqueCidrs = new HashSet<>();
		for (String sourceCidr : sourceCidrs) {
			if (isBlank(sourceCidr) || !uniqueCidrs.add(sourceCidr)) {
				return false;
			}
			try {
				if (!LegacyCompatibilityCidr.parse(sourceCidr).isHostCidr()) {
					return false;
				}
			} catch (IllegalArgumentException exception) {
				return false;
			}
		}
		return true;
	}

	private boolean matchesClientSource(LegacyDoorLockCompatibilityProperties.Client client, String sourceIp) {
		List<String> sourceCidrs = client.getSourceCidrs();
		if (sourceCidrs == null || sourceCidrs.isEmpty()) {
			return false;
		}
		for (String sourceCidr : sourceCidrs) {
			try {
				if (LegacyCompatibilityCidr.parse(sourceCidr).matches(sourceIp)) {
					return true;
				}
			} catch (IllegalArgumentException exception) {
				// 配置项非法时按该来源不匹配处理，避免无效 CIDR 放大为允许访问。
			}
		}
		return false;
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private boolean isCompatibilityPathCandidate(String requestUri) {
		if (requestUri == null) {
			return false;
		}
		for (String path : COMPATIBILITY_PATHS) {
			if (requestUri.equals(path) || requestUri.startsWith(path + "/") || requestUri.startsWith(path + ";")) {
				return true;
			}
		}
		return false;
	}
}
