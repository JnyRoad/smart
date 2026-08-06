package com.tce.smart.gateway.filter;

import com.tce.smart.common.core.security.LegacyCompatibilityProof;
import com.tce.smart.gateway.config.LegacyDoorLockCompatibilityProperties;
import org.junit.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;

/**
 * DoorLock 旧接口只能由 Gateway 根据 TCP 来源生成内部证明，外部请求头不得参与身份判定。
 */
public class LegacyDoorLockCompatibilityGlobalFilterTest {

	private static final String SIGNATURE_KEY = "gateway-test-signature-key";
	private static final String STAFF_PATH = "/dormitory/staff/remote/to/lock";
	private static final String X_FORWARDED_FOR = "X-Forwarded-For";

	@Test
	public void shouldAllowSecondConfiguredCallerAndReplaceForgedProofHeaders() {
		LegacyDoorLockCompatibilityGlobalFilter filter = new LegacyDoorLockCompatibilityGlobalFilter(enabledProperties());
		MockServerHttpRequest request = request(HttpMethod.GET, STAFF_PATH + "?b=2&a=1", "10.13.21.31",
			LegacyCompatibilityProof.HEADER_CALLER_ID, "forged-caller",
			LegacyCompatibilityProof.HEADER_SIGNATURE, "forged-signature",
			LegacyDoorLockCompatibilityGlobalFilter.PROXY_SOURCE_IP_HEADER, "10.13.21.99",
			X_FORWARDED_FOR, "10.13.21.99");

		AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
		filter.filter(legacyExchange(request), capture(forwarded)).block();

		ServerWebExchange forwardedExchange = forwarded.get();
		assertNotNull(forwardedExchange);
		ServerHttpRequest forwardedRequest = forwardedExchange.getRequest();
		assertEquals("door-lock-secondary", forwardedRequest.getHeaders().getFirst(LegacyCompatibilityProof.HEADER_CALLER_ID));
		assertEquals("10.13.21.31", forwardedRequest.getHeaders().getFirst(LegacyCompatibilityProof.HEADER_SOURCE_IP));
		assertNull(forwardedRequest.getHeaders().getFirst(LegacyDoorLockCompatibilityGlobalFilter.PROXY_SOURCE_IP_HEADER));
		assertEquals("10.13.21.99", forwardedRequest.getHeaders().getFirst(X_FORWARDED_FOR));

		LegacyCompatibilityProof.Claims claims = claimsFrom(forwardedRequest);
		assertTrue(LegacyCompatibilityProof.verify(claims,
			forwardedRequest.getHeaders().getFirst(LegacyCompatibilityProof.HEADER_SIGNATURE), SIGNATURE_KEY));
	}

	@Test
	public void shouldRejectUnlistedTcpPeer() {
		LegacyDoorLockCompatibilityGlobalFilter filter = new LegacyDoorLockCompatibilityGlobalFilter(enabledProperties());
		MockServerWebExchange exchange = legacyExchange(request(HttpMethod.GET, STAFF_PATH, "10.13.21.99"));

		AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
		filter.filter(exchange, capture(forwarded)).block();

		assertNull(forwarded.get());
		assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
	}

	@Test
	public void shouldRejectPostAndReservedNearPath() {
		LegacyDoorLockCompatibilityGlobalFilter filter = new LegacyDoorLockCompatibilityGlobalFilter(enabledProperties());
		MockServerWebExchange postExchange = legacyExchange(request(HttpMethod.POST, STAFF_PATH, "10.13.21.30"));
		MockServerWebExchange nearPathExchange = legacyExchange(request(HttpMethod.GET, STAFF_PATH + "/detail", "10.13.21.30"));

		filter.filter(postExchange, capture(new AtomicReference<>())).block();
		filter.filter(nearPathExchange, capture(new AtomicReference<>())).block();

		assertEquals(HttpStatus.FORBIDDEN, postExchange.getResponse().getStatusCode());
		assertEquals(HttpStatus.FORBIDDEN, nearPathExchange.getResponse().getStatusCode());
	}

	@Test
	public void shouldRejectWhenCompatibilityIsDisabled() {
		LegacyDoorLockCompatibilityProperties properties = enabledProperties();
		properties.setEnabled(false);
		LegacyDoorLockCompatibilityGlobalFilter filter = new LegacyDoorLockCompatibilityGlobalFilter(properties);
		MockServerWebExchange exchange = legacyExchange(request(HttpMethod.GET, STAFF_PATH, "10.13.21.30"));

		filter.filter(exchange, capture(new AtomicReference<>())).block();

		assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
	}

	@Test
	public void shouldRejectByDefaultAndWhenCallerListIsEmpty() {
		LegacyDoorLockCompatibilityGlobalFilter defaultFilter = new LegacyDoorLockCompatibilityGlobalFilter(
			new LegacyDoorLockCompatibilityProperties());
		LegacyDoorLockCompatibilityProperties emptyCallerProperties = enabledProperties();
		emptyCallerProperties.setClients(Collections.emptyList());
		LegacyDoorLockCompatibilityGlobalFilter emptyCallerFilter = new LegacyDoorLockCompatibilityGlobalFilter(emptyCallerProperties);
		MockServerWebExchange defaultExchange = legacyExchange(request(HttpMethod.GET, STAFF_PATH, "10.13.21.30"));
		MockServerWebExchange emptyCallerExchange = legacyExchange(request(HttpMethod.GET, STAFF_PATH, "10.13.21.30"));

		defaultFilter.filter(defaultExchange, capture(new AtomicReference<>())).block();
		emptyCallerFilter.filter(emptyCallerExchange, capture(new AtomicReference<>())).block();

		assertEquals(HttpStatus.FORBIDDEN, defaultExchange.getResponse().getStatusCode());
		assertEquals(HttpStatus.FORBIDDEN, emptyCallerExchange.getResponse().getStatusCode());
		assertEquals(-999, emptyCallerFilter.getOrder());
	}

	@Test
	public void shouldRejectProxySourceHeaderAndRequireConfiguredTcpPeer() {
		LegacyDoorLockCompatibilityProperties properties = enabledProperties();
		LegacyDoorLockCompatibilityGlobalFilter filter = new LegacyDoorLockCompatibilityGlobalFilter(properties);
		MockServerHttpRequest trustedProxyRequest = request(HttpMethod.GET, STAFF_PATH, "10.0.20.10",
			LegacyDoorLockCompatibilityGlobalFilter.PROXY_SOURCE_IP_HEADER, "10.13.21.31");
		MockServerWebExchange exchange = legacyExchange(trustedProxyRequest);

		filter.filter(exchange, capture(new AtomicReference<>())).block();

		assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
	}

	@Test
	public void shouldRejectBroadCallerCidrInsteadOfTreatingItAsAnIpList() {
		LegacyDoorLockCompatibilityProperties properties = enabledProperties();
		properties.getClients().get(0).setSourceCidrs(Collections.singletonList("10.13.21.0/24"));
		LegacyDoorLockCompatibilityGlobalFilter filter = new LegacyDoorLockCompatibilityGlobalFilter(properties);
		MockServerWebExchange exchange = legacyExchange(request(HttpMethod.GET, STAFF_PATH, "10.13.21.30"));

		filter.filter(exchange, capture(new AtomicReference<>())).block();

		assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
	}

	@Test
	public void shouldRejectNonPlatformOriginAndInvalidProofInputWithoutForwarding() {
		LegacyDoorLockCompatibilityGlobalFilter filter = new LegacyDoorLockCompatibilityGlobalFilter(enabledProperties());
		MockServerHttpRequest platformPathRequest = request(HttpMethod.GET, STAFF_PATH, "10.13.21.30");
		MockServerWebExchange nonPlatformExchange = MockServerWebExchange.from(platformPathRequest);
		LegacyDoorLockCompatibilityProperties invalidProofProperties = enabledProperties();
		invalidProofProperties.setKeyId("key\ninvalid");
		LegacyDoorLockCompatibilityGlobalFilter invalidProofFilter = new LegacyDoorLockCompatibilityGlobalFilter(invalidProofProperties);
		MockServerWebExchange invalidProofExchange = legacyExchange(request(HttpMethod.GET, STAFF_PATH, "10.13.21.30"));

		filter.filter(nonPlatformExchange, capture(new AtomicReference<>())).block();
		invalidProofFilter.filter(invalidProofExchange, capture(new AtomicReference<>())).block();

		assertEquals(HttpStatus.FORBIDDEN, nonPlatformExchange.getResponse().getStatusCode());
		assertEquals(HttpStatus.FORBIDDEN, invalidProofExchange.getResponse().getStatusCode());
	}

	private LegacyCompatibilityProof.Claims claimsFrom(ServerHttpRequest request) {
		return LegacyCompatibilityProof.Claims.of(LegacyCompatibilityProof.VERSION,
			request.getHeaders().getFirst(LegacyCompatibilityProof.HEADER_KEY_ID),
			request.getHeaders().getFirst(LegacyCompatibilityProof.HEADER_CALLER_ID),
			request.getHeaders().getFirst(LegacyCompatibilityProof.HEADER_SOURCE_IP),
			request.getMethodValue(), request.getURI().getRawPath(), request.getURI().getRawQuery(),
			Long.parseLong(request.getHeaders().getFirst(LegacyCompatibilityProof.HEADER_ISSUED_AT)),
			Long.parseLong(request.getHeaders().getFirst(LegacyCompatibilityProof.HEADER_EXPIRES_AT)),
			request.getHeaders().getFirst(LegacyCompatibilityProof.HEADER_NONCE));
	}

	private GatewayFilterChain capture(AtomicReference<ServerWebExchange> forwarded) {
		return exchange -> {
			forwarded.set(exchange);
			return Mono.empty();
		};
	}

	private MockServerHttpRequest request(HttpMethod method, String path, String peerIp, String... headers) {
		MockServerHttpRequest.BaseBuilder<?> builder = MockServerHttpRequest.method(method, "http://gateway" + path)
			.remoteAddress(new InetSocketAddress(peerIp, 23000));
		for (int index = 0; index < headers.length; index += 2) {
			builder.header(headers[index], headers[index + 1]);
		}
		return builder.build();
	}

	private MockServerWebExchange legacyExchange(MockServerHttpRequest request) {
		MockServerWebExchange exchange = MockServerWebExchange.from(request);
		String query = request.getURI().getRawQuery();
		URI originalUrl = URI.create("http://gateway/platform" + request.getURI().getRawPath()
			+ (query == null ? "" : "?" + query));
		exchange.getAttributes().put(GATEWAY_ORIGINAL_REQUEST_URL_ATTR,
			new LinkedHashSet<>(Collections.singletonList(originalUrl)));
		return exchange;
	}

	private LegacyDoorLockCompatibilityProperties enabledProperties() {
		LegacyDoorLockCompatibilityProperties properties = new LegacyDoorLockCompatibilityProperties();
		properties.setEnabled(true);
		properties.setKeyId("door-lock-v1");
		properties.setSignatureKey(SIGNATURE_KEY);
		properties.setSignatureTtlSeconds(30);
		properties.setClients(Arrays.asList(client("door-lock-primary", "10.13.21.30/32"),
			client("door-lock-secondary", "10.13.21.31/32")));
		return properties;
	}

	private LegacyDoorLockCompatibilityProperties.Client client(String id, String... sourceCidrs) {
		LegacyDoorLockCompatibilityProperties.Client client = new LegacyDoorLockCompatibilityProperties.Client();
		client.setId(id);
		client.setSourceCidrs(Arrays.asList(sourceCidrs));
		return client;
	}
}
