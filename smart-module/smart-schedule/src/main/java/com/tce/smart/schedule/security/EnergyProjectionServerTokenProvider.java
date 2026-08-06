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

/**
 * 为无 HTTP 请求上下文的能耗投影调度任务申请并缓存 server scope 的 Bearer 令牌。
 */
public class EnergyProjectionServerTokenProvider {

	private final EnergyProjectionOAuthProperties properties;
	private final RestOperations restOperations;
	private final Clock clock;
	private volatile CachedToken cachedToken;

	public EnergyProjectionServerTokenProvider(EnergyProjectionOAuthProperties properties, RestOperations restOperations,
			Clock clock) {
		this.properties = properties;
		this.restOperations = restOperations;
		this.clock = clock;
	}

	/**
	 * 获取可用于 Feign 调用的 Authorization 请求头；配置或授权失败时安全抛错，不发送匿名内部请求。
	 */
	public String authorizationHeader() {
		Instant now = clock.instant();
		CachedToken current = cachedToken;
		if (current != null && current.isUsable(now)) {
			return current.authorizationHeader;
		}
		synchronized (this) {
			now = clock.instant();
			current = cachedToken;
			if (current != null && current.isUsable(now)) {
				return current.authorizationHeader;
			}
			CachedToken refreshed = requestToken(now);
			cachedToken = refreshed;
			return refreshed.authorizationHeader;
		}
	}

	/**
	 * 按 OAuth2 client_credentials 规范向授权端点申请 server scope 令牌。
	 */
	@SuppressWarnings("rawtypes")
	private CachedToken requestToken(Instant now) {
		validateConfiguration();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set(HttpHeaders.AUTHORIZATION, basicAuthorization(properties.getClientId(), properties.getClientSecret()));
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "client_credentials");
		form.add("scope", properties.getScope());
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
	private void validateConfiguration() {
		if (isBlank(properties.getAccessTokenUri())) {
			throw new IllegalStateException("能耗投影 OAuth access-token-uri 未配置");
		}
		if (isBlank(properties.getClientId())) {
			throw new IllegalStateException("能耗投影 OAuth client-id 未配置");
		}
		if (isBlank(properties.getClientSecret())) {
			throw new IllegalStateException("能耗投影 OAuth client-secret 未配置");
		}
		if (isBlank(properties.getScope())) {
			throw new IllegalStateException("能耗投影 OAuth scope 未配置");
		}
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
