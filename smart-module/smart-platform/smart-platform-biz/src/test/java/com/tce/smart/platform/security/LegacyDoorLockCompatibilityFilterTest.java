package com.tce.smart.platform.security;

import com.tce.smart.common.core.security.LegacyCompatibilityProof;
import com.tce.smart.common.data.security.RedisNonceReplayGuard;
import com.tce.smart.platform.conf.LegacyDoorLockCompatibilityProperties;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * DoorLock 同路径兼容入口的安全契约：即使 OAuth 白名单放行，也只能接受
 * Gateway 生成且未被消费的证明，不能把匿名请求直接交给控制器。
 */
public class LegacyDoorLockCompatibilityFilterTest {

	private static final String KEY_ID = "door-lock-v1";
	private static final String SIGNATURE_KEY = "test-only-shared-secret";

	@Test
	public void acceptsConfiguredCallerAndPublishesItsParkScope() throws Exception {
		LegacyDoorLockCompatibilityProperties properties = configuredProperties();
		RedisNonceReplayGuard replayGuard = Mockito.mock(RedisNonceReplayGuard.class);
		Mockito.when(replayGuard.reserve(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong()))
				.thenReturn(RedisNonceReplayGuard.ReserveResult.ACCEPTED);
		MockHttpServletRequest request = signedRequest("/park/tolock/dormitory/allList", null, "10.13.21.31", "lock-b");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		new LegacyDoorLockCompatibilityFilter(properties, replayGuard).doFilter(request, response, chain);

		assertEquals(200, response.getStatus());
		assertEquals(Arrays.asList(21, 22), LegacyDoorLockCallerContext.require(request).getParkIds());
		assertEquals(request, chain.getRequest());
	}

	@Test
	public void rejectsConfiguredCallerWhenSourceIpIsOutsideItsCidr() throws Exception {
		LegacyDoorLockCompatibilityProperties properties = configuredProperties();
		RedisNonceReplayGuard replayGuard = Mockito.mock(RedisNonceReplayGuard.class);
		MockHttpServletRequest request = signedRequest("/dormitory/staff/remote/to/lock", "parkId=21", "10.13.21.99", "lock-b");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		new LegacyDoorLockCompatibilityFilter(properties, replayGuard).doFilter(request, response, chain);

		assertEquals(403, response.getStatus());
		assertNull(chain.getRequest());
		Mockito.verifyZeroInteractions(replayGuard);
	}

	@Test
	public void rejectsBroadSourceCidrInsteadOfTreatingItAsAnIpList() throws Exception {
		LegacyDoorLockCompatibilityProperties properties = configuredProperties();
		properties.getClients().get(0).setSourceCidrs(Arrays.asList("10.13.21.0/24"));
		RedisNonceReplayGuard replayGuard = Mockito.mock(RedisNonceReplayGuard.class);
		MockHttpServletRequest request = signedRequest("/staff/define/badge", "badge=A001", "10.13.21.30", "lock-a");
		MockHttpServletResponse response = new MockHttpServletResponse();

		new LegacyDoorLockCompatibilityFilter(properties, replayGuard).doFilter(request, response, new MockFilterChain());

		assertEquals(403, response.getStatus());
		Mockito.verifyZeroInteractions(replayGuard);
	}

	@Test
	public void rejectsReplayAndDoesNotCallTheController() throws Exception {
		LegacyDoorLockCompatibilityProperties properties = configuredProperties();
		RedisNonceReplayGuard replayGuard = Mockito.mock(RedisNonceReplayGuard.class);
		Mockito.when(replayGuard.reserve(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong()))
				.thenReturn(RedisNonceReplayGuard.ReserveResult.REPLAYED);
		MockHttpServletRequest request = signedRequest("/staff/define/badge", "badge=A001", "10.13.21.30", "lock-a");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		new LegacyDoorLockCompatibilityFilter(properties, replayGuard).doFilter(request, response, chain);

		assertEquals(403, response.getStatus());
		assertNull(chain.getRequest());
	}

	@Test
	public void returnsServiceUnavailableWhenReplayProtectionCannotReserveNonce() throws Exception {
		LegacyDoorLockCompatibilityProperties properties = configuredProperties();
		RedisNonceReplayGuard replayGuard = Mockito.mock(RedisNonceReplayGuard.class);
		Mockito.when(replayGuard.reserve(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong()))
				.thenReturn(RedisNonceReplayGuard.ReserveResult.UNAVAILABLE);
		MockHttpServletRequest request = signedRequest("/staff/define/badge", "badge=A001", "10.13.21.30", "lock-a");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		new LegacyDoorLockCompatibilityFilter(properties, replayGuard).doFilter(request, response, chain);

		assertEquals(503, response.getStatus());
		assertNull(chain.getRequest());
	}

	@Test
	public void rejectsExpiredProofBeforeConsumingNonce() throws Exception {
		LegacyDoorLockCompatibilityProperties properties = configuredProperties();
		RedisNonceReplayGuard replayGuard = Mockito.mock(RedisNonceReplayGuard.class);
		MockHttpServletRequest request = signedRequestWithWindow("/staff/define/badge", "badge=A001", "10.13.21.30",
				"lock-a", -40, -20);
		MockHttpServletResponse response = new MockHttpServletResponse();

		new LegacyDoorLockCompatibilityFilter(properties, replayGuard).doFilter(request, response, new MockFilterChain());

		assertEquals(403, response.getStatus());
		Mockito.verifyZeroInteractions(replayGuard);
	}

	@Test
	public void rejectsTamperedQueryBeforeConsumingNonce() throws Exception {
		LegacyDoorLockCompatibilityProperties properties = configuredProperties();
		RedisNonceReplayGuard replayGuard = Mockito.mock(RedisNonceReplayGuard.class);
		MockHttpServletRequest request = signedRequest("/staff/define/badge", "badge=A001", "10.13.21.30", "lock-a");
		request.setQueryString("badge=OTHER");
		MockHttpServletResponse response = new MockHttpServletResponse();

		new LegacyDoorLockCompatibilityFilter(properties, replayGuard).doFilter(request, response, new MockFilterChain());

		assertEquals(403, response.getStatus());
		Mockito.verifyZeroInteractions(replayGuard);
	}

	@Test
	public void rejectsNonGetAndNearPathInsteadOfLettingIgnoreUrlsBypassTheFilter() throws Exception {
		LegacyDoorLockCompatibilityProperties properties = configuredProperties();
		RedisNonceReplayGuard replayGuard = Mockito.mock(RedisNonceReplayGuard.class);
		MockHttpServletRequest post = signedRequest("/staff/define/badge", "badge=A001", "10.13.21.30", "lock-a");
		post.setMethod("POST");
		MockHttpServletResponse postResponse = new MockHttpServletResponse();
		MockHttpServletRequest nearPath = signedRequest("/staff/define/badge", "badge=A001", "10.13.21.30", "lock-a");
		nearPath.setRequestURI("/staff/define/badge/extra");
		MockHttpServletResponse nearPathResponse = new MockHttpServletResponse();

		new LegacyDoorLockCompatibilityFilter(properties, replayGuard).doFilter(post, postResponse, new MockFilterChain());
		new LegacyDoorLockCompatibilityFilter(properties, replayGuard).doFilter(nearPath, nearPathResponse, new MockFilterChain());

		assertEquals(403, postResponse.getStatus());
		assertEquals(403, nearPathResponse.getStatus());
		Mockito.verifyZeroInteractions(replayGuard);
	}

	private MockHttpServletRequest signedRequest(String path, String query, String sourceIp, String callerId) {
		return signedRequestWithWindow(path, query, sourceIp, callerId, -1, 20);
	}

	private MockHttpServletRequest signedRequestWithWindow(String path, String query, String sourceIp, String callerId,
			long issuedOffsetSeconds, long expiresOffsetSeconds) {
		long now = System.currentTimeMillis() / 1000L;
		String nonce = "nonce-" + callerId + "-" + path.replace('/', '_');
		LegacyCompatibilityProof.Claims claims = LegacyCompatibilityProof.Claims.of(
				LegacyCompatibilityProof.VERSION, KEY_ID, callerId, sourceIp, "GET", path,
				query == null ? "" : query, now + issuedOffsetSeconds, now + expiresOffsetSeconds, nonce);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
		request.setQueryString(query);
		request.addHeader(LegacyCompatibilityProof.HEADER_KEY_ID, KEY_ID);
		request.addHeader(LegacyCompatibilityProof.HEADER_CALLER_ID, callerId);
		request.addHeader(LegacyCompatibilityProof.HEADER_SOURCE_IP, sourceIp);
		request.addHeader(LegacyCompatibilityProof.HEADER_ISSUED_AT, String.valueOf(now + issuedOffsetSeconds));
		request.addHeader(LegacyCompatibilityProof.HEADER_EXPIRES_AT, String.valueOf(now + expiresOffsetSeconds));
		request.addHeader(LegacyCompatibilityProof.HEADER_NONCE, nonce);
		request.addHeader(LegacyCompatibilityProof.HEADER_SIGNATURE,
				LegacyCompatibilityProof.sign(claims, SIGNATURE_KEY));
		return request;
	}

	private LegacyDoorLockCompatibilityProperties configuredProperties() {
		LegacyDoorLockCompatibilityProperties properties = new LegacyDoorLockCompatibilityProperties();
		properties.setEnabled(true);
		properties.setKeyId(KEY_ID);
		properties.setSignatureKey(SIGNATURE_KEY);
		properties.setMaxClockSkewSeconds(5);
		properties.setMaxTtlSeconds(30);
		properties.setClients(Arrays.asList(
				client("lock-a", "10.13.21.30/32", 11),
				client("lock-b", "10.13.21.31/32", 21, 22)));
		return properties;
	}

	private LegacyDoorLockCompatibilityProperties.Client client(String id, String sourceCidr, Integer... parkIds) {
		LegacyDoorLockCompatibilityProperties.Client client = new LegacyDoorLockCompatibilityProperties.Client();
		client.setId(id);
		client.setSourceCidrs(Arrays.asList(sourceCidr));
		client.setParkIds(Arrays.asList(parkIds));
		return client;
	}
}
