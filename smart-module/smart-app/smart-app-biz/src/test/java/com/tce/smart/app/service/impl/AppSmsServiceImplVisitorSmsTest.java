package com.tce.smart.app.service.impl;

import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * 匿名访客短信的限流契约：同一手机号在冷却窗口内重复提交不得再次触发短信供应商。
 */
public class AppSmsServiceImplVisitorSmsTest {

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

		assertTrue(service.sendVisitorSmsCode("13800138000"));

		Mockito.verifyZeroInteractions(remoteSmsManageService);
	}
}
