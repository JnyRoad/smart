package com.tce.smart.app.service.fore;

import com.tce.smart.app.service.AppSmsService;
import com.tce.smart.app.service.fore.impl.PasswordServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.platform.api.dto.resp.InternalStaffPhoneRespDTO;
import com.tce.smart.platform.api.feign.RemoteStaffInternalService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 密码找回 challenge 的抗枚举回归测试。
 */
public class PasswordResetChallengeTest {

	@Test
	public void createChallengeNeverReturnsPhoneAndKeepsItsStateServerSide() {
		StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		RemoteStaffInternalService staffService = Mockito.mock(RemoteStaffInternalService.class);
		PasswordServiceImpl service = passwordService(redis, values, staffService);
		InternalStaffPhoneRespDTO phone = new InternalStaffPhoneRespDTO();
		phone.setPhone("13800138000");
		Mockito.when(values.increment(Mockito.anyString(), Mockito.eq(1L))).thenReturn(1L);
		Mockito.when(staffService.getPasswordPhone(Mockito.eq("8031249"), Mockito.anyString(), Mockito.anyString(),
				Mockito.eq("password-reset"))).thenReturn(Result.success(phone));

		String challengeId = service.createPasswordResetChallenge("8031249");

		assertNotEquals("13800138000", challengeId);
		assertFalse(challengeId.contains("8031249"));
		ArgumentCaptor<String> state = ArgumentCaptor.forClass(String.class);
		Mockito.verify(values).set(Mockito.anyString(), state.capture(), Mockito.eq(600L), Mockito.eq(TimeUnit.SECONDS));
		assertTrue("完整手机号只能存在服务端 challenge 中", state.getValue().contains("13800138000"));
	}

	@Test
	public void inactiveChallengeAcknowledgesSmsRequestWithoutCallingSmsProvider() {
		StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		AppSmsService smsService = Mockito.mock(AppSmsService.class);
		PasswordServiceImpl service = passwordService(redis, values, Mockito.mock(RemoteStaffInternalService.class));
		ReflectionTestUtils.setField(service, "appSmsService", smsService);
		Mockito.when(values.get(Mockito.anyString())).thenReturn(
				"{\"purpose\":\"password-reset\",\"active\":false,\"sendAttempts\":0,\"verifyAttempts\":0}");

		assertTrue(service.sendSmsCode("opaque-challenge"));
		Mockito.verifyZeroInteractions(smsService);
	}

	@Test
	public void slowProviderKeepsChallengeReservedAndConcurrentRequestNeverCallsProvider() throws Exception {
		StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		AppSmsService smsService = Mockito.mock(AppSmsService.class);
		PasswordServiceImpl service = passwordService(redis, values, Mockito.mock(RemoteStaffInternalService.class));
		ReflectionTestUtils.setField(service, "appSmsService", smsService);
		AtomicBoolean reserved = new AtomicBoolean(false);
		AtomicInteger providerCalls = new AtomicInteger();
		CountDownLatch providerEntered = new CountDownLatch(1);
		CountDownLatch releaseProvider = new CountDownLatch(1);
		Mockito.when(redis.execute(Mockito.any(), Mockito.anyList(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString()))
				.thenAnswer(invocation -> {
					if (String.class.equals(((org.springframework.data.redis.core.script.DefaultRedisScript<?>) invocation.getArgument(0)).getResultType())) {
						return reserved.compareAndSet(false, true)
								? "{\"purpose\":\"password-reset\",\"phone\":\"13800138000\",\"active\":true,\"sendAttempts\":1,\"sendState\":\"SENDING\",\"sendReservationId\":\"reservation\"}"
								: null;
					}
					return 1L;
				});
		Mockito.doAnswer(invocation -> {
			providerCalls.incrementAndGet();
			providerEntered.countDown();
			releaseProvider.await(2, TimeUnit.SECONDS);
			return Boolean.TRUE;
		}).when(smsService).sendSmsCode("13800138000");

		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch start = new CountDownLatch(1);
		Future<Boolean> first = executor.submit(() -> {
			start.await(2, TimeUnit.SECONDS);
			return service.sendSmsCode("opaque-challenge");
		});
		Future<Boolean> second = executor.submit(() -> {
			start.await(2, TimeUnit.SECONDS);
			return service.sendSmsCode("opaque-challenge");
		});
		start.countDown();
		assertTrue("预约赢家必须进入 provider", providerEntered.await(2, TimeUnit.SECONDS));
		Thread.sleep(100L);
		assertEquals("同一 challenge 的并发请求最多一次下发", 1, providerCalls.get());
		releaseProvider.countDown();
		assertTrue(first.get(2, TimeUnit.SECONDS));
		assertTrue(second.get(2, TimeUnit.SECONDS));
		executor.shutdownNow();
		Mockito.verify(smsService, Mockito.times(1)).sendSmsCode("13800138000");
	}

	@Test
	public void providerFailureReleasesReservationForControlledRetry() {
		StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		AppSmsService smsService = Mockito.mock(AppSmsService.class);
		PasswordServiceImpl service = passwordService(redis, values, Mockito.mock(RemoteStaffInternalService.class));
		ReflectionTestUtils.setField(service, "appSmsService", smsService);
		AtomicInteger reservation = new AtomicInteger();
		List<String> completionStates = new CopyOnWriteArrayList<>();
		Mockito.when(redis.execute(Mockito.any(), Mockito.anyList(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenAnswer(invocation -> {
			int attempt = reservation.incrementAndGet();
			return "{\"purpose\":\"password-reset\",\"phone\":\"13800138000\",\"active\":true,\"sendAttempts\":"
					+ attempt + ",\"sendState\":\"SENDING\",\"sendReservationId\":\"reservation-" + attempt + "\"}";
		});
		Mockito.when(redis.execute(Mockito.any(), Mockito.anyList(), Mockito.anyString(), Mockito.anyString()))
				.thenAnswer(invocation -> {
					completionStates.add(invocation.getArgument(3));
					return 1L;
				});
		Mockito.when(smsService.sendSmsCode("13800138000"))
				.thenThrow(new RuntimeException("provider failure")).thenReturn(Boolean.TRUE);

		assertTrue(service.sendSmsCode("opaque-challenge"));
		assertTrue(service.sendSmsCode("opaque-challenge"));

		Mockito.verify(smsService, Mockito.times(2)).sendSmsCode("13800138000");
		assertEquals("失败必须释放预约，成功必须终结预约", java.util.Arrays.asList("READY", "SENT"), completionStates);
	}

	@Test
	public void concurrentChallengeLoserCannotExchangeAnotherRequestsVerifiedChallenge() {
		StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		AppSmsService smsService = Mockito.mock(AppSmsService.class);
		PasswordServiceImpl service = passwordService(redis, values, Mockito.mock(RemoteStaffInternalService.class));
		ReflectionTestUtils.setField(service, "appSmsService", smsService);
		String state = "{\"purpose\":\"password-reset\",\"badge\":\"8031249\",\"phone\":\"13800138000\",\"active\":true,\"sendAttempts\":0,\"verifyAttempts\":0}";
		Mockito.when(values.get(Mockito.anyString())).thenReturn(state);
		Mockito.when(smsService.verifySmsCode("13800138000", "123456")).thenReturn(Boolean.TRUE);
		Mockito.when(redis.execute(Mockito.any(), Mockito.anyList(), Mockito.anyString())).thenReturn(0L);

		try {
			service.verifySmsCode("opaque-challenge", "123456");
			fail("并发输家不得兑换已被另一请求消费的 challenge");
		} catch (TCEException expected) {
			// 预期：Lua compare-and-delete 返回 0，不能签发改密授权。
		}
		Mockito.verify(redis).execute(Mockito.any(), Mockito.anyList(), Mockito.eq(state));
	}

	private PasswordServiceImpl passwordService(StringRedisTemplate redis, ValueOperations<String, String> values,
			RemoteStaffInternalService staffService) {
		PasswordServiceImpl service = new PasswordServiceImpl();
		Mockito.when(redis.opsForValue()).thenReturn(values);
		ReflectionTestUtils.setField(service, "stringRedisTemplate", redis);
		ReflectionTestUtils.setField(service, "remoteStaffInternalService", staffService);
		return service;
	}
}
