package com.tce.smart.gateway.filter;

import com.tce.smart.common.core.constant.SecurityConstants;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/** 标准 API 根路径不得被历史服务前缀剥离逻辑改写。 */
public class SmartRequestGlobalFilterTest {
	@Test
	public void preservesCanonicalApiPathAndRemovesForgedFromHeader() {
		MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/me")
			.header(SecurityConstants.FROM, "forged").build();
		AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();
		new SmartRequestGlobalFilter().filter(MockServerWebExchange.from(request), exchange -> {
			downstream.set(exchange); return Mono.empty();
		}).block();
		assertEquals("/api/v1/me", downstream.get().getRequest().getURI().getPath());
		assertNull(downstream.get().getRequest().getHeaders().getFirst(SecurityConstants.FROM));
	}

	@Test
	public void keepsLegacyServicePrefixStrippingForExistingRoutes() {
		MockServerHttpRequest request = MockServerHttpRequest.get("/platform/articlesrelease/page").build();
		AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();
		new SmartRequestGlobalFilter().filter(MockServerWebExchange.from(request), exchange -> {
			downstream.set(exchange); return Mono.empty();
		}).block();
		assertEquals("/articlesrelease/page", downstream.get().getRequest().getURI().getPath());
	}
}
