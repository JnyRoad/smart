package com.tce.smart.platform.support;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** RedisMutexLock 单测：验证抢占与原子释放语义 */
public class RedisMutexLockTest {

	private StringRedisTemplate redisTemplate;
	private ValueOperations<String, String> valueOps;
	private RedisMutexLock lock;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		redisTemplate = mock(StringRedisTemplate.class);
		valueOps = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOps);
		lock = new RedisMutexLock(redisTemplate);
	}

	@Test
	public void acquire_success_returnsToken() {
		// setIfAbsent 成功 → 返回非空 token
		when(valueOps.setIfAbsent(eq("k"), anyString(), eq(600L), eq(TimeUnit.SECONDS))).thenReturn(true);
		String token = lock.acquire("k", 600);
		assertNotNull(token);
	}

	@Test
	public void acquire_held_returnsNull() {
		// 已被占用 → 返回 null
		when(valueOps.setIfAbsent(eq("k"), anyString(), eq(600L), eq(TimeUnit.SECONDS))).thenReturn(false);
		assertNull(lock.acquire("k", 600));
	}

	@Test
	public void release_executesLuaWithToken() {
		// 释放走 Lua 脚本，key/token 原样传入
		lock.release("k", "t1");
		verify(redisTemplate).execute(any(RedisScript.class), eq(Collections.singletonList("k")), eq("t1"));
	}

	@Test
	public void release_nullToken_noop() {
		// token 为空直接返回，不触发 Redis 调用
		lock.release("k", null);
		verify(redisTemplate, never()).execute(any(RedisScript.class), anyList(), any());
	}
}
