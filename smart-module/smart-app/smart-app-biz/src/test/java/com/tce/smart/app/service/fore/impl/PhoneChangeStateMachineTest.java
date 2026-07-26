package com.tce.smart.app.service.fore.impl;

import com.tce.smart.app.controller.fore.SettingServiceController;
import com.tce.smart.app.service.AppSmsService;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.dto.resp.InternalStaffPhoneRespDTO;
import com.tce.smart.platform.api.feign.RemoteStaffInternalService;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** 换绑手机号的 Redis 状态机必须绑定账号、号码摘要和用途，并保证并发请求只能消费一次。 */
public class PhoneChangeStateMachineTest {

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void oldPhoneVerificationCreatesShortLivedHashedServerState() {
		StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		AppSmsService sms = Mockito.mock(AppSmsService.class);
		RemoteStaffInternalService staff = staffPhone("employee-7", "13800138000");
		SettingServiceImpl service = service(redis, values, sms, staff);
		login(7, "employee-7");
		Mockito.when(sms.consumePhoneChangeSmsCode(7, "old", "13800138000", "123456")).thenReturn(Boolean.TRUE);

		assertTrue(service.verifyOldPhoneCode("123456"));

		ArgumentCaptor<String> state = ArgumentCaptor.forClass(String.class);
		Mockito.verify(values).set(Mockito.eq("smart_app:phone-change:old-verified:7"), state.capture(),
				Mockito.eq(600L), Mockito.eq(TimeUnit.SECONDS));
		assertTrue(state.getValue().contains("\"purpose\":\"phone-change\""));
		assertTrue(state.getValue().contains("\"userId\":7"));
		assertTrue(state.getValue().contains("\"state\":\"OLD_VERIFIED\""));
		assertFalse("Redis 换绑状态不得保存完整旧手机号", state.getValue().contains("13800138000"));
	}

	@Test
	public void authenticatedFlowBindsNewPhoneBeforeSmsCanBeConfirmed() {
		StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		AppSmsService sms = Mockito.mock(AppSmsService.class);
		SettingServiceImpl service = service(redis, values, sms, staffPhone("employee-7", "13800138000"));
		login(7, "employee-7");
		Mockito.when(redis.execute(Mockito.any(), Mockito.anyList(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(1L);
		Mockito.when(sms.sendPhoneChangeSmsCode(7, "new", "13900139000")).thenReturn(Boolean.TRUE);

		assertTrue(service.sendNewPhoneCode("13900139000"));

		ArgumentCaptor<List> keys = ArgumentCaptor.forClass(List.class);
		Mockito.verify(redis, Mockito.times(2)).execute(Mockito.any(), keys.capture(), Mockito.eq("phone-change"), Mockito.eq("7"),
				Mockito.anyString(), Mockito.anyString());
		assertEquals(Collections.singletonList("smart_app:phone-change:old-verified:7"), keys.getAllValues().get(0));
		assertEquals(Collections.singletonList("smart_app:phone-change:old-verified:7"), keys.getAllValues().get(1));
		Mockito.verify(sms).sendPhoneChangeSmsCode(7, "new", "13900139000");
	}

	@Test
	public void failedOrThrownNewPhoneSmsNeverMarksStateAsSent() {
		assertNewPhoneSmsFailureLeavesStateSending(Boolean.FALSE, false);
		assertNewPhoneSmsFailureLeavesStateSending(null, false);
		assertNewPhoneSmsFailureLeavesStateSending(null, true);
	}

	private void assertNewPhoneSmsFailureLeavesStateSending(Boolean providerResult, boolean throwsProviderError) {
		StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		AppSmsService sms = Mockito.mock(AppSmsService.class);
		SettingServiceImpl service = service(redis, values, sms, staffPhone("employee-7", "13800138000"));
		login(7, "employee-7");
		Mockito.when(redis.execute(Mockito.any(), Mockito.anyList(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(1L);
		if (throwsProviderError) {
			Mockito.when(sms.sendPhoneChangeSmsCode(7, "new", "13900139000"))
					.thenThrow(new TCEException("provider unavailable"));
		} else {
			Mockito.when(sms.sendPhoneChangeSmsCode(7, "new", "13900139000")).thenReturn(providerResult);
		}

		try {
			service.sendNewPhoneCode("13900139000");
			fail("短信未成功受理时不得进入可确认状态");
		} catch (TCEException expected) {
			Mockito.verify(redis, Mockito.times(1)).execute(Mockito.any(), Mockito.anyList(), Mockito.anyString(),
					Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		}
	}

	@Test
	public void missingOldVerificationNeverCallsNewPhoneSmsProvider() {
		StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		AppSmsService sms = Mockito.mock(AppSmsService.class);
		SettingServiceImpl service = service(redis, values, sms, staffPhone("employee-7", "13800138000"));
		login(7, "employee-7");
		Mockito.when(redis.execute(Mockito.any(), Mockito.anyList(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(0L);

		try {
			service.sendNewPhoneCode("13900139000");
			fail("旧手机号未验证时不得下发新手机号验证码");
		} catch (TCEException expected) {
			Mockito.verify(sms, Mockito.never()).sendPhoneChangeSmsCode(Mockito.anyInt(), Mockito.eq("new"), Mockito.anyString());
		}
	}

	@Test
	public void atomicConsumeAllowsOnlyOneConcurrentWinnerAndRejectsAnotherAccount() throws Exception {
		StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		SettingServiceImpl service = service(redis, values, Mockito.mock(AppSmsService.class),
				Mockito.mock(RemoteStaffInternalService.class));
		AtomicBoolean reservationAvailable = new AtomicBoolean(true);
		Mockito.when(redis.execute(Mockito.any(), Mockito.anyList(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenAnswer(invocation -> {
			List<String> keys = invocation.getArgument(1);
			String stateUserId = invocation.getArgument(3);
			return keys.equals(Collections.singletonList("smart_app:phone-change:old-verified:7")) && "7".equals(stateUserId)
					&& reservationAvailable.compareAndSet(true, false) ? 1L : 0L;
		});

		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch start = new CountDownLatch(1);
		Future<Boolean> first = executor.submit(() -> consumeAfterStart(service, start));
		Future<Boolean> second = executor.submit(() -> consumeAfterStart(service, start));
		start.countDown();
		assertTrue(first.get(2, TimeUnit.SECONDS) ^ second.get(2, TimeUnit.SECONDS));
		executor.shutdownNow();
		assertFalse("另一账号不得领取账号 7 的授权", service.consumePhoneChange(8, "old-hash", "new-hash"));
	}

	@Test
	public void unauthenticatedRequestFailsBeforeAnyStaffOrSmsCall() {
		StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		AppSmsService sms = Mockito.mock(AppSmsService.class);
		RemoteStaffInternalService staff = Mockito.mock(RemoteStaffInternalService.class);
		SettingServiceImpl service = service(redis, values, sms, staff);

		try {
			service.sendOldPhoneCode();
			fail("未认证请求不得触发手机号换绑短信");
		} catch (TCEException expected) {
			Mockito.verifyZeroInteractions(staff, sms);
		}
	}

	@Test
	public void phoneChangeRoutesRejectGetAndExposeOnlyScopedPostActions() {
		for (Method method : SettingServiceController.class.getDeclaredMethods()) {
			if ("sendOldPhoneCode".equals(method.getName()) || "verifyOldPhoneCode".equals(method.getName())
					|| "sendNewPhoneCode".equals(method.getName()) || "confirmNewPhone".equals(method.getName())) {
				assertTrue("换绑动作必须是 POST", method.isAnnotationPresent(PostMapping.class));
				assertFalse("换绑动作不得保留 GET 映射", method.isAnnotationPresent(GetMapping.class));
			}
		}
	}

	private Boolean consumeAfterStart(SettingServiceImpl service, CountDownLatch start) throws Exception {
		start.await(2, TimeUnit.SECONDS);
		return service.consumePhoneChange(7, "old-hash", "new-hash");
	}

	private SettingServiceImpl service(StringRedisTemplate redis, ValueOperations<String, String> values, AppSmsService sms,
			RemoteStaffInternalService staff) {
		SettingServiceImpl service = new SettingServiceImpl();
		Mockito.when(redis.opsForValue()).thenReturn(values);
		ReflectionTestUtils.setField(service, "stringRedisTemplate", redis);
		ReflectionTestUtils.setField(service, "appSmsService", sms);
		ReflectionTestUtils.setField(service, "remoteStaffInternalService", staff);
		return service;
	}

	private RemoteStaffInternalService staffPhone(String badge, String phoneNumber) {
		RemoteStaffInternalService staff = Mockito.mock(RemoteStaffInternalService.class);
		InternalStaffPhoneRespDTO phone = new InternalStaffPhoneRespDTO();
		phone.setPhone(phoneNumber);
		Mockito.when(staff.getPasswordPhone(Mockito.eq(badge), Mockito.anyString(), Mockito.anyString(),
				Mockito.eq("self-phone-verify"))).thenReturn(Result.success(phone));
		return staff;
	}

	private void login(Integer userId, String badge) {
		SmartUser user = new SmartUser(userId, 1, badge, Collections.singletonList(1), "N/A", true, true, true,
				true, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "N/A", user.getAuthorities()));
	}
}
