package com.tce.smart.common.data.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Redis nonce 只能被成功占位一次；Redis 故障绝不能降级为允许重放。 */
public class RedisNonceReplayGuardTest {

	private ValueOperations<String, String> valueOperations;
	private RedisNonceReplayGuard guard;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
		valueOperations = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		guard = new RedisNonceReplayGuard(redisTemplate);
	}

	@Test
	public void reserveAcceptsTheFirstNonceWithoutWritingItIntoTheRedisKey() {
		when(valueOperations.setIfAbsent(anyString(), eq("1"), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(true);

		RedisNonceReplayGuard.ReserveResult result = guard.reserve("v1", "lock-a", "nonce-sensitive-value", 30);

		assertEquals(RedisNonceReplayGuard.ReserveResult.ACCEPTED, result);
		ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
		verify(valueOperations).setIfAbsent(keyCaptor.capture(), eq("1"), eq(30L), eq(TimeUnit.SECONDS));
		assertFalse(keyCaptor.getValue().contains("nonce-sensitive-value"));
	}

	@Test
	public void reserveRejectsAReplayWhenRedisReportsTheNonceAlreadyExists() {
		when(valueOperations.setIfAbsent(anyString(), eq("1"), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(false);

		assertEquals(RedisNonceReplayGuard.ReserveResult.REPLAYED,
				guard.reserve("v1", "lock-a", "nonce-1", 30));
	}

	@Test
	public void reserveReportsUnavailableWhenRedisCannotConfirmTheNonce() {
		when(valueOperations.setIfAbsent(anyString(), eq("1"), anyLong(), eq(TimeUnit.SECONDS)))
				.thenThrow(new RuntimeException("redis unavailable"));

		assertEquals(RedisNonceReplayGuard.ReserveResult.UNAVAILABLE,
				guard.reserve("v1", "lock-a", "nonce-1", 30));
	}
}
