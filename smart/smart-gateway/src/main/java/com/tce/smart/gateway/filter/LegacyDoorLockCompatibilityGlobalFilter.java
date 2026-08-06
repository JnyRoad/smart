package com.tce.smart.gateway.filter;

import com.tce.smart.common.core.security.LegacyCompatibilityCidr;
import com.tce.smart.common.core.security.LegacyCompatibilityProof;
import com.tce.smart.gateway.config.LegacyDoorLockCompatibilityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.List;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;

/**
 * 为不可修改的 DoorLock 旧调用保留原 URL，并在 Gateway 处完成来源网络校验。
 *
 * 该过滤器必须运行在 {@link SmartRequestGlobalFilter} 之后，因此签名中的 path 是
 * 后端服务可见的本地路径。外部所有 Legacy 头会先被删除，只有校验通过时才由
 * Gateway 重建签名头；绝不直接使用 X-Forwarded-For 作为身份来源。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LegacyDoorLockCompatibilityGlobalFilter implements GlobalFilter, Ordered {

	/**
	 * 历史部署可能遗留的代理来源头。该过滤器绝不信任它，只会在签名前剥离，
	 * 以免客户端或上游代理将它误作为身份凭据继续传递。
	 */
	public static final String PROXY_SOURCE_IP_HEADER = "X-Smart-Proxy-Source-Ip";
	private static final List<String> LEGACY_PATHS = Collections.unmodifiableList(Arrays.asList(
		"/dormitory/staff/remote/to/lock",
		"/park/tolock/dormitory/allList",
		"/staff/define/badge"));
	private static final int MIN_SIGNATURE_TTL_SECONDS = 1;
	private static final int MAX_SIGNATURE_TTL_SECONDS = 300;
	private static final int NONCE_BYTES = 24;

	private final LegacyDoorLockCompatibilityProperties properties;
	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest originalRequest = exchange.getRequest();
		// 来源身份只取 Gateway 与调用方之间的 TCP 对端地址；随后剥离所有外部兼容头。
		String sourceIp = resolveSourceIp(originalRequest);
		ServerHttpRequest sanitizedRequest = removeExternalCompatibilityHeaders(originalRequest);
		String path = sanitizedRequest.getURI().getRawPath();
		if (!isReservedLegacyPath(path)) {
			return chain.filter(exchange.mutate().request(sanitizedRequest).build());
		}
		if (!isPlatformOriginalPath(exchange, path) || !isExactLegacyGet(sanitizedRequest)) {
			return reject(exchange);
		}

		LegacyDoorLockCompatibilityProperties.Client caller = findCaller(sourceIp);
		if (caller == null || sourceIp == null || !hasCompleteConfiguration()) {
			return reject(exchange);
		}
		String nonce = nonce();
		long issuedAt = Instant.now().getEpochSecond();
		long expiresAt = issuedAt + properties.getSignatureTtlSeconds();
		LegacyCompatibilityProof.Claims claims;
		String signature;
		try {
			claims = LegacyCompatibilityProof.Claims.of(LegacyCompatibilityProof.VERSION, properties.getKeyId(), caller.getId(), sourceIp,
				sanitizedRequest.getMethodValue(), path, sanitizedRequest.getURI().getRawQuery(), issuedAt, expiresAt, nonce);
			signature = LegacyCompatibilityProof.sign(claims, properties.getSignatureKey());
		} catch (RuntimeException exception) {
			// 非法原始查询参数不能把兼容入口降级成 500，也不能绕过签名校验。
			log.warn("遗留 DoorLock 请求无法生成签名证明，已拒绝");
			return reject(exchange);
		}
		ServerHttpRequest signedRequest = sanitizedRequest.mutate().headers(headers -> {
			headers.set(LegacyCompatibilityProof.HEADER_KEY_ID, properties.getKeyId());
			headers.set(LegacyCompatibilityProof.HEADER_CALLER_ID, caller.getId());
			headers.set(LegacyCompatibilityProof.HEADER_SOURCE_IP, sourceIp);
			headers.set(LegacyCompatibilityProof.HEADER_ISSUED_AT, String.valueOf(issuedAt));
			headers.set(LegacyCompatibilityProof.HEADER_EXPIRES_AT, String.valueOf(expiresAt));
			headers.set(LegacyCompatibilityProof.HEADER_NONCE, nonce);
			headers.set(LegacyCompatibilityProof.HEADER_SIGNATURE, signature);
		}).build();
		return chain.filter(exchange.mutate().request(signedRequest).build());
	}

	private boolean hasCompleteConfiguration() {
		if (!properties.isEnabled()
			|| !StringUtils.hasText(properties.getKeyId())
			|| !StringUtils.hasText(properties.getSignatureKey())
			|| properties.getSignatureTtlSeconds() < MIN_SIGNATURE_TTL_SECONDS
			|| properties.getSignatureTtlSeconds() > MAX_SIGNATURE_TTL_SECONDS
			|| properties.getClients() == null || properties.getClients().isEmpty()) {
			return false;
		}
		return areValidClients(properties.getClients());
	}

	private boolean areValidClients(List<LegacyDoorLockCompatibilityProperties.Client> clients) {
		for (LegacyDoorLockCompatibilityProperties.Client client : clients) {
			if (client == null || !StringUtils.hasText(client.getId()) || client.getSourceCidrs() == null
				|| client.getSourceCidrs().isEmpty() || !areValidHostCidrs(client.getSourceCidrs())) {
				return false;
			}
		}
		return true;
	}

	private boolean areValidHostCidrs(List<String> cidrs) {
		if (cidrs == null) {
			return false;
		}
		try {
			for (String cidr : cidrs) {
				if (!LegacyCompatibilityCidr.parse(cidr).isHostCidr()) {
					return false;
				}
			}
			return true;
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}

	private LegacyDoorLockCompatibilityProperties.Client findCaller(String sourceIp) {
		if (!hasCompleteConfiguration()) {
			return null;
		}
		if (sourceIp == null) {
			return null;
		}
		LegacyDoorLockCompatibilityProperties.Client matched = null;
		for (LegacyDoorLockCompatibilityProperties.Client client : properties.getClients()) {
			if (client == null || !StringUtils.hasText(client.getId()) || !matchesAny(client.getSourceCidrs(), sourceIp)) {
				continue;
			}
			// 重叠 CIDR 会使调用方身份不可判定，按拒绝处理而不是随意选中一个。
			if (matched != null) {
				log.warn("遗留 DoorLock 来源命中多个调用方配置，sourceIp={}", sourceIp);
				return null;
			}
			matched = client;
		}
		return matched;
	}

	private String resolveSourceIp(ServerHttpRequest request) {
		InetSocketAddress remoteAddress = request.getRemoteAddress();
		if (remoteAddress == null || remoteAddress.getAddress() == null) {
			return null;
		}
		// 不接受任何反代转发的来源头。只有 TCP 对端本身在精确主机名单内才可签名，
		// 避免反代没有清除客户端头时伪造白名单来源。
		return remoteAddress.getAddress().getHostAddress();
	}

	private boolean matchesAny(List<String> cidrs, String sourceIp) {
		if (cidrs == null || cidrs.isEmpty()) {
			return false;
		}
		try {
			for (String cidr : cidrs) {
				if (LegacyCompatibilityCidr.parse(cidr).matches(sourceIp)) {
					return true;
				}
			}
			return false;
		} catch (IllegalArgumentException exception) {
			// 配置含非法 CIDR 时拒绝该请求，不能回退到宽松匹配。
			log.warn("遗留 DoorLock CIDR 配置非法，拒绝兼容调用");
			return false;
		}
	}

	private ServerHttpRequest removeExternalCompatibilityHeaders(ServerHttpRequest request) {
		return request.mutate().headers(headers -> {
			List<String> headerNames = new ArrayList<>(headers.keySet());
			for (String headerName : headerNames) {
				if (headerName.regionMatches(true, 0, LegacyCompatibilityProof.HEADER_PREFIX, 0,
					LegacyCompatibilityProof.HEADER_PREFIX.length())) {
					headers.remove(headerName);
				}
			}
			headers.remove(PROXY_SOURCE_IP_HEADER);
		}).build();
	}

	private boolean isReservedLegacyPath(String path) {
		if (path == null) {
			return false;
		}
		for (String legacyPath : LEGACY_PATHS) {
			if (path.startsWith(legacyPath)) {
				return true;
			}
		}
		return false;
	}

	private boolean isExactLegacyGet(ServerHttpRequest request) {
		return request.getMethod() == HttpMethod.GET && LEGACY_PATHS.contains(request.getURI().getRawPath());
	}

	private boolean isPlatformOriginalPath(ServerWebExchange exchange, String servicePath) {
		Object originalUrls = exchange.getAttribute(GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
		if (!(originalUrls instanceof Set) || ((Set<?>) originalUrls).isEmpty()) {
			return false;
		}
		Object firstUrl = ((Set<?>) originalUrls).iterator().next();
		if (!(firstUrl instanceof URI)) {
			return false;
		}
		return ("/platform" + servicePath).equals(((URI) firstUrl).getRawPath());
	}

	private String nonce() {
		byte[] bytes = new byte[NONCE_BYTES];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private Mono<Void> reject(ServerWebExchange exchange) {
		exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
		return exchange.getResponse().setComplete();
	}

	/** SmartRequestGlobalFilter 为 -1000，本过滤器必须在其后接收服务本地路径。 */
	@Override
	public int getOrder() {
		return -999;
	}
}
