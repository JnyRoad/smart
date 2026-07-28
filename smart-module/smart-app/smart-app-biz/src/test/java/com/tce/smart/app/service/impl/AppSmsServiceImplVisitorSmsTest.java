package com.tce.smart.app.service.impl;

import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.tool.exception.TCEException;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * 匿名访客短信的限流契约：同一手机号在冷却窗口内重复提交不得再次触发短信供应商。
 */
public class AppSmsServiceImplVisitorSmsTest {

	private static final String MOBILE = "13800138000";
	private static final String SMS_CODE = "123456";
	private static final String GENERIC_SMS_KEY = "smart_app:wechat:smscode:" + MOBILE;
	private static final String VISITOR_SMS_KEY = "smart_app:visitor:sms:code:" + MOBILE;
	private static final String VISITOR_FAILURE_KEY = "smart_app:visitor:sms:verify-failure:" + MOBILE;

	@Test
	public void visitorSmsSendDoesNotCallProviderWhenMobileIsCoolingDown() {
		AppSmsServiceImpl service = new AppSmsServiceImpl();
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		RemoteSmsManageService remoteSmsManageService = Mockito.mock(RemoteSmsManageService.class);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		Mockito.when(valueOperations.setIfAbsent(Mockito.anyString(), Mockito.eq("1"), Mockito.eq(60L),
				Mockito.eq(TimeUnit.SECONDS))).thenReturn(Boolean.FALSE);
		ReflectionTestUtils.setField(service, "stringRedisTemplate", redisTemplate);
		ReflectionTestUtils.setField(service, "remoteSmsManageService", remoteSmsManageService);

		assertTrue(service.sendVisitorSmsCode(MOBILE));

		Mockito.verifyZeroInteractions(remoteSmsManageService);
	}

	/**
	 * 成功校验必须经由访客专用、原子的 compare-and-delete，而不是读取通用 OTP 后保留原值。
	 * 否则同一验证码可同时用于访客记录查询和货车预约，形成重放入口。
	 */
	@Test
	public void visitorSmsVerificationConsumesDedicatedCodeAfterSuccessfulVerification() {
		AppSmsServiceImpl service = new AppSmsServiceImpl();
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		// 使旧实现可以正常返回，断言才会精确暴露它遗漏的原子消费步骤。
		Mockito.when(valueOperations.get(GENERIC_SMS_KEY)).thenReturn("{\"smsCode\":\"123456\"}");
		Mockito.when(redisTemplate.execute(Mockito.any(DefaultRedisScript.class),
				Mockito.eq(Arrays.asList(VISITOR_SMS_KEY, VISITOR_FAILURE_KEY)), Mockito.eq(SMS_CODE),
				Mockito.eq(5L), Mockito.eq(600L))).thenReturn(1L);
		ReflectionTestUtils.setField(service, "stringRedisTemplate", redisTemplate);

		assertTrue(service.verifyVisitorSmsCode(MOBILE, SMS_CODE));

		Mockito.verify(redisTemplate).execute(Mockito.any(DefaultRedisScript.class),
				Mockito.eq(Arrays.asList(VISITOR_SMS_KEY, VISITOR_FAILURE_KEY)), Mockito.eq(SMS_CODE),
				Mockito.eq(5L), Mockito.eq(600L));
	}

	/** 第六次失败尝试即使猜中正确验证码也必须被拒绝，避免在限额边界绕过失败次数限制。 */
	@Test
	public void visitorSmsVerificationRejectsCorrectCodeAfterFailureLimitIsReached() {
		AppSmsServiceImpl service = new AppSmsServiceImpl();
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		Mockito.when(valueOperations.get(GENERIC_SMS_KEY)).thenReturn("{\"smsCode\":\"123456\"}");
		Mockito.when(redisTemplate.execute(Mockito.any(DefaultRedisScript.class),
				Mockito.eq(Arrays.asList(VISITOR_SMS_KEY, VISITOR_FAILURE_KEY)), Mockito.eq(SMS_CODE),
				Mockito.eq(5L), Mockito.eq(600L))).thenReturn(-1L);
		ReflectionTestUtils.setField(service, "stringRedisTemplate", redisTemplate);

		expectInvalidCode(() -> service.verifyVisitorSmsCode(MOBILE, SMS_CODE));

		Mockito.verify(redisTemplate).execute(Mockito.any(DefaultRedisScript.class),
				Mockito.eq(Arrays.asList(VISITOR_SMS_KEY, VISITOR_FAILURE_KEY)), Mockito.eq(SMS_CODE),
				Mockito.eq(5L), Mockito.eq(600L));
	}

	private void expectInvalidCode(ThrowingRunnable runnable) {
		try {
			runnable.run();
			throw new AssertionError("达到失败次数上限后必须拒绝验证码校验");
		} catch (TCEException expected) {
			// 匿名入口只返回统一错误，避免泄露具体失败原因。
		} catch (Exception error) {
			throw new AssertionError(error);
		}
	}

	@FunctionalInterface
	private interface ThrowingRunnable {
		void run() throws Exception;
	}
}
