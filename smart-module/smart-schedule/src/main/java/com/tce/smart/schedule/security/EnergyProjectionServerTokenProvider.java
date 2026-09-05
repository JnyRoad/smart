package com.tce.smart.schedule.security;

import com.tce.smart.schedule.config.EnergyProjectionOAuthProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 为无 HTTP 请求上下文的内部调度任务申请并缓存 Bearer 令牌，默认使用 server 授权。
 *
 * <p>缓存按实际申请的 scope 隔离，兼容旧配置时不串用不同授权域的令牌。</p>
 */
public class EnergyProjectionServerTokenProvider {

	private final EnergyProjectionOAuthProperties properties;
	private final RestOperations restOperations;
	private final Clock clock;
	private final ConcurrentMap<String, CachedToken> cachedTokens = new ConcurrentHashMap<>();

	public EnergyProjectionServerTokenProvider(EnergyProjectionOAuthProperties properties, RestOperations restOperations,
			Clock clock) {
		this.properties = properties;
		this.restOperations = restOperations;
		this.clock = clock;
	}

	/**
	 * 获取配置的通用服务 Authorization 请求头；缺少凭据或换取令牌失败时抛错。
	 */
	public String authorizationHeader() {
		return authorizationHeader(properties.getScope());
	}

	/**
	 * 获取能耗投影内部调用令牌，默认申请 server，并沿用显式配置的历史授权域。
	 */
	public String energyProjectionAuthorizationHeader() {
		return authorizationHeader(properties.getEnergyProjectionRunScope());
	}

	/**
	 * 获取指定 capability scope 的 Authorization 请求头；配置或授权失败时安全抛错，
	 * 不发送匿名内部请求。不同 scope 的令牌独立缓存，避免权限串用。
	 */
	public String authorizationHeader(String capabilityScope) {
		String normalizedScope = normalizeCapabilityScope(capabilityScope);
		Instant now = clock.instant();
		CachedToken current = cachedTokens.get(normalizedScope);
		if (current != null && current.isUsable(now)) {
			return current.authorizationHeader;
		}
		synchronized (this) {
			now = clock.instant();
			current = cachedTokens.get(normalizedScope);
			if (current != null && current.isUsable(now)) {
				return current.authorizationHeader;
			}
			CachedToken refreshed = requestToken(now, normalizedScope);
			cachedTokens.put(normalizedScope, refreshed);
			return refreshed.authorizationHeader;
		}
	}

	/**
	 * 按 OAuth2 client_credentials 规范向授权端点申请指定 capability scope 令牌。
	 */
	@SuppressWarnings("rawtypes")
	private CachedToken requestToken(Instant now, String capabilityScope) {
		validateConfiguration(capabilityScope);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set(HttpHeaders.AUTHORIZATION, basicAuthorization(properties.getClientId(), properties.getClientSecret()));
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "client_credentials");
		form.add("scope", capabilityScope);
		ResponseEntity<Map> response;
		try {
			response = restOperations.exchange(properties.getAccessTokenUri(), HttpMethod.POST,
					new HttpEntity<MultiValueMap<String, String>>(form, headers), Map.class);
		} catch (RuntimeException e) {
			throw new IllegalStateException("能耗投影调度无法获取 OAuth 访问令牌", e);
		}
		if (response == null || response.getStatusCode() == null || !response.getStatusCode().is2xxSuccessful()) {
			throw new IllegalStateException("能耗投影调度获取 OAuth 访问令牌失败");
		}
		Map responseBody = response.getBody();
		String accessToken = responseBody == null ? null : valueAsString(responseBody.get("access_token"));
		if (isBlank(accessToken)) {
			throw new IllegalStateException("能耗投影调度获取 OAuth 访问令牌失败：响应缺少 access_token");
		}
		long expiresInSeconds = responseBody == null ? 0L : valueAsLong(responseBody.get("expires_in"));
		long refreshBeforeExpirySeconds = Math.max(0L, properties.getRefreshBeforeExpirySeconds());
		long reusableSeconds = expiresInSeconds > 0L ? Math.max(0L, expiresInSeconds - refreshBeforeExpirySeconds) : 0L;
		return new CachedToken("Bearer " + accessToken, now.plusSeconds(reusableSeconds));
	}

	/**
	 * 配置不完整时不向平台内部端点发出无凭证调用。
	 */
	private void validateConfiguration(String capabilityScope) {
		if (isBlank(properties.getAccessTokenUri())) {
			throw new IllegalStateException("能耗投影 OAuth access-token-uri 未配置");
		}
		if (isBlank(properties.getClientId())) {
			throw new IllegalStateException("能耗投影 OAuth client-id 未配置");
		}
		if (isBlank(properties.getClientSecret())) {
			throw new IllegalStateException("能耗投影 OAuth client-secret 未配置");
		}
		if (isBlank(capabilityScope)) {
			throw new IllegalStateException("能耗投影 OAuth scope 未配置");
		}
	}

	/**
	 * 统一去除配置值两侧空白，避免同一 capability 因配置格式差异产生重复缓存项。
	 */
	private String normalizeCapabilityScope(String capabilityScope) {
		if (isBlank(capabilityScope)) {
			throw new IllegalStateException("能耗投影 OAuth scope 未配置");
		}
		return capabilityScope.trim();
	}

	private String basicAuthorization(String clientId, String clientSecret) {
		String value = clientId + ":" + clientSecret;
		return "Basic " + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private String valueAsString(Object value) {
		return value == null ? null : String.valueOf(value).trim();
	}

	private long valueAsLong(Object value) {
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		try {
			return value == null ? 0L : Long.parseLong(String.valueOf(value));
		} catch (NumberFormatException ignored) {
			return 0L;
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	/**
	 * 缓存值只在预留刷新窗口之前可复用，避免临近过期的令牌被发送到远端。
	 */
	private static class CachedToken {
		private final String authorizationHeader;
		private final Instant reusableUntil;

		private CachedToken(String authorizationHeader, Instant reusableUntil) {
			this.authorizationHeader = authorizationHeader;
			this.reusableUntil = reusableUntil;
		}

		private boolean isUsable(Instant now) {
			return now.isBefore(reusableUntil);
		}
	}
}
