package com.tce.smart.schedule.service.comm.impl;

import com.tce.smart.tool.enums.TimerTaskEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SwitchServiceImplTest {

	@Test
	public void processUsesCustomLockTtlWhenProvided() throws Exception {
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SwitchServiceImpl service = new SwitchServiceImpl();
		setField(service, "redisTemplate", redisTemplate);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		Mockito.when(valueOperations.setIfAbsent(TimerTaskEnum.ADMITTANCE_UPDATE_OA.getKey(),
				TimerTaskEnum.ADMITTANCE_UPDATE_OA.getDesc(), 5L, TimeUnit.MINUTES)).thenReturn(Boolean.TRUE);

		Boolean acquired = service.process(TimerTaskEnum.ADMITTANCE_UPDATE_OA, 5L, TimeUnit.MINUTES);

		Assert.assertTrue(acquired);
		Mockito.verify(valueOperations).setIfAbsent(TimerTaskEnum.ADMITTANCE_UPDATE_OA.getKey(),
				TimerTaskEnum.ADMITTANCE_UPDATE_OA.getDesc(), 5L, TimeUnit.MINUTES);
	}

	@Test
	public void acquireReturnsOwnerTokenAndReleaseDeletesOnlyOwnedLock() throws Exception {
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SwitchServiceImpl service = new SwitchServiceImpl();
		setField(service, "redisTemplate", redisTemplate);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		Mockito.when(valueOperations.setIfAbsent(Mockito.eq(TimerTaskEnum.ADMITTANCE_UPDATE_OA.getKey()),
				Mockito.anyString(), Mockito.eq(30L), Mockito.eq(TimeUnit.MINUTES))).thenReturn(Boolean.TRUE);

		String token = service.acquire(TimerTaskEnum.ADMITTANCE_UPDATE_OA, 30L, TimeUnit.MINUTES);

		Assert.assertNotNull(token);
		service.release(TimerTaskEnum.ADMITTANCE_UPDATE_OA, token);
		Mockito.verify(redisTemplate).execute(Mockito.any(DefaultRedisScript.class),
				Mockito.eq(Collections.singletonList(TimerTaskEnum.ADMITTANCE_UPDATE_OA.getKey())),
				Mockito.eq(token));
		Mockito.verify(redisTemplate, Mockito.never()).delete(Mockito.anyString());
		Mockito.verify(valueOperations, Mockito.never()).get(Mockito.anyString());
	}

	@Test
	public void releaseUsesAtomicCompareAndDeleteScript() throws Exception {
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SwitchServiceImpl service = new SwitchServiceImpl();
		setField(service, "redisTemplate", redisTemplate);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);

		service.release(TimerTaskEnum.ADMITTANCE_UPDATE_OA, "current-token");

		Mockito.verify(redisTemplate).execute(Mockito.any(DefaultRedisScript.class),
				Mockito.eq(Collections.singletonList(TimerTaskEnum.ADMITTANCE_UPDATE_OA.getKey())),
				Mockito.eq("current-token"));
		Mockito.verify(redisTemplate, Mockito.never()).delete(Mockito.anyString());
		Mockito.verify(valueOperations, Mockito.never()).get(Mockito.anyString());
	}

	@Test
	public void releaseDoesNotThrowWhenRedisUnlockFails() throws Exception {
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		SwitchServiceImpl service = new SwitchServiceImpl();
		setField(service, "redisTemplate", redisTemplate);
		Mockito.when(redisTemplate.execute(Mockito.any(DefaultRedisScript.class),
				Mockito.eq(Collections.singletonList(TimerTaskEnum.ADMITTANCE_UPDATE_OA.getKey())),
				Mockito.eq("current-token"))).thenThrow(new RuntimeException("redis down"));

		service.release(TimerTaskEnum.ADMITTANCE_UPDATE_OA, "current-token");

		Mockito.verify(redisTemplate, Mockito.times(3)).execute(Mockito.any(DefaultRedisScript.class),
				Mockito.eq(Collections.singletonList(TimerTaskEnum.ADMITTANCE_UPDATE_OA.getKey())),
				Mockito.eq("current-token"));
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
