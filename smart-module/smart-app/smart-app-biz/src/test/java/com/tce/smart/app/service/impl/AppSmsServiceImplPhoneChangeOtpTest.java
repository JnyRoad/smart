package com.tce.smart.app.service.impl;

import com.tce.smart.tool.exception.TCEException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** 换绑 OTP 必须隔离于通用短信命名空间，并由 Redis compare-and-delete 一次性消费。 */
public class AppSmsServiceImplPhoneChangeOtpTest {

	@Test
	public void oldAndNewPhoneChangeOtpsAreAtomicOneTimeCredentials() {
		StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
		AppSmsServiceImpl service = service(redis);
		AtomicBoolean oldAvailable = new AtomicBoolean(true);
		AtomicBoolean newAvailable = new AtomicBoolean(true);
		Mockito.when(redis.execute(Mockito.any(), Mockito.anyList(), Mockito.anyString())).thenAnswer(invocation -> {
			List<String> keys = invocation.getArgument(1);
			String key = keys.get(0);
			return key.contains(":old:") ? oldAvailable.compareAndSet(true, false) ? 1L : 0L
					: newAvailable.compareAndSet(true, false) ? 1L : 0L;
		});

		assertTrue(service.consumePhoneChangeSmsCode(7, "old", "13800138000", "123456"));
		assertReplayFails(service, "old", "13800138000", "123456");
		assertTrue(service.consumePhoneChangeSmsCode(7, "new", "13900139000", "654321"));
		assertReplayFails(service, "new", "13900139000", "654321");
	}

	@Test
	public void dedicatedOtpKeyBindsUserStageAndPhoneHashInsteadOfRawMobile() {
		StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
		AppSmsServiceImpl service = service(redis);
		Mockito.when(redis.execute(Mockito.any(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);

		assertTrue(service.consumePhoneChangeSmsCode(7, "old", "13800138000", "123456"));

		ArgumentCaptor<List> keys = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<DefaultRedisScript> script = ArgumentCaptor.forClass(DefaultRedisScript.class);
		Mockito.verify(redis).execute(script.capture(), keys.capture(), Mockito.eq("123456"));
		String key = String.valueOf(keys.getValue().get(0));
		assertTrue(key.startsWith("smart_app:phone-change:sms:7:old:"));
		assertFalse(key.contains("13800138000"));
		assertTrue(script.getValue().getScriptAsString().contains("redis.call('del', KEYS[1])"));
	}

	private void assertReplayFails(AppSmsServiceImpl service, String stage, String mobile, String smsCode) {
		try {
			service.consumePhoneChangeSmsCode(7, stage, mobile, smsCode);
			fail("已消费的换绑 OTP 不得重放");
		} catch (TCEException expected) {
			// Redis compare-and-delete 返回 0，服务端统一拒绝。
		}
	}

	private AppSmsServiceImpl service(StringRedisTemplate redis) {
		AppSmsServiceImpl service = new AppSmsServiceImpl();
		ReflectionTestUtils.setField(service, "stringRedisTemplate", redis);
		return service;
	}
}
