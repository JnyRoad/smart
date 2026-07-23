package com.tce.smart.app.service.fore;

import com.tce.smart.app.service.AppSmsService;
import com.tce.smart.app.service.fore.impl.PasswordServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.resp.InternalStaffPhoneRespDTO;
import com.tce.smart.platform.api.feign.RemoteStaffInternalService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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

	private PasswordServiceImpl passwordService(StringRedisTemplate redis, ValueOperations<String, String> values,
			RemoteStaffInternalService staffService) {
		PasswordServiceImpl service = new PasswordServiceImpl();
		Mockito.when(redis.opsForValue()).thenReturn(values);
		ReflectionTestUtils.setField(service, "stringRedisTemplate", redis);
		ReflectionTestUtils.setField(service, "remoteStaffInternalService", staffService);
		return service;
	}
}
