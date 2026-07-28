package com.tce.smart.platform.service.admittance.impl;

import com.tce.smart.app.api.dto.InternalSmsVerifyReqDTO;
import com.tce.smart.app.api.feign.RemoteAppSmsService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.admittance.SaveAdmittanceCarApplyReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.VisitorTruckSmsVerifyReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorTruckProofRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@SuppressWarnings({"rawtypes", "unchecked"})
public class VisitorTruckProofServiceImplTest {

	/**
	 * 货车预约是匿名访客入口，必须使用访客专用的可消费校验契约；
	 * 通用内部校验成功不能再被当成可重放的预约凭据来源。
	 */
	@Test
	public void verifySmsUsesVisitorOnlyVerificationContract() {
		List<String> invokedMethods = new ArrayList<>();
		RemoteAppSmsService smsService = visitorOnlySmsService(invokedMethods);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(values);
		VisitorTruckProofServiceImpl service = service(smsService, redisTemplate,
				Mockito.mock(SmtAdmittanceApplyService.class));
		VisitorTruckSmsVerifyReqDTO request = new VisitorTruckSmsVerifyReqDTO();
		request.setMobile("13712341234");
		request.setSmsCode("123456");

		VisitorTruckProofRespDTO response = service.verifySms(request);

		Assert.assertEquals("proof-fixed", response.getProof());
		Assert.assertEquals(Collections.singletonList("verifyVisitorSmsCode"), invokedMethods);
	}

	@Test
	public void verifySmsIssuesShortLivedProofBoundToNormalizedMobile() {
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(values);
		Mockito.when(smsService.verifyVisitorSmsCode(Mockito.any(InternalSmsVerifyReqDTO.class),
				Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(Boolean.TRUE));
		VisitorTruckProofServiceImpl service = service(smsService, redisTemplate, Mockito.mock(SmtAdmittanceApplyService.class));
		VisitorTruckSmsVerifyReqDTO request = new VisitorTruckSmsVerifyReqDTO();
		request.setMobile("137 1234 1234");
		request.setSmsCode("123456");

		VisitorTruckProofRespDTO response = service.verifySms(request);

		Assert.assertEquals("proof-fixed", response.getProof());
		Assert.assertFalse(response.toString().contains("13712341234"));
		Mockito.verify(smsService).verifyVisitorSmsCode(Mockito.argThat(candidate -> candidate != null
				&& "13712341234".equals(candidate.getMobile()) && "123456".equals(candidate.getSmsCode())),
				Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Mockito.verify(values).set("smart:admittance:visitor-truck:proof:proof-fixed", "13712341234", 5L,
				TimeUnit.MINUTES);
	}

	@Test
	public void verifySmsRejectsInvalidRemoteVerification() {
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		Mockito.when(smsService.verifyVisitorSmsCode(Mockito.any(InternalSmsVerifyReqDTO.class), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(Result.success(Boolean.FALSE));
		VisitorTruckProofServiceImpl service = service(smsService, redisTemplate, Mockito.mock(SmtAdmittanceApplyService.class));
		VisitorTruckSmsVerifyReqDTO request = new VisitorTruckSmsVerifyReqDTO();
		request.setMobile("13712341234");
		request.setSmsCode("123456");

		expectSmartException(() -> service.verifySms(request));
		Mockito.verify(redisTemplate, Mockito.never()).opsForValue();
	}

	@Test
	public void causeOptionsAcceptActiveProofWithoutConsumingIt() {
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(values);
		Mockito.when(values.get("smart:admittance:visitor-truck:proof:proof-active")).thenReturn("13712341234");
		VisitorTruckProofServiceImpl service = service(smsService, redisTemplate, Mockito.mock(SmtAdmittanceApplyService.class));

		service.assertActiveProof("proof-active");
		service.assertActiveProof("proof-active");

		Mockito.verify(values, Mockito.times(2)).get("smart:admittance:visitor-truck:proof:proof-active");
		Mockito.verify(redisTemplate, Mockito.never()).execute(Mockito.any(), Mockito.anyList(), Mockito.any());
	}

	@Test
	public void applyRejectsMissingProof() {
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		SmtAdmittanceApplyService applyService = Mockito.mock(SmtAdmittanceApplyService.class);
		VisitorTruckProofServiceImpl service = service(smsService, redisTemplate, applyService);
		SaveAdmittanceCarApplyReqDTO request = carApply("13900000000");

		expectSmartException(() -> service.apply(null, request));
		Mockito.verify(applyService, Mockito.never()).saveAdmittanceCarApply(Mockito.any(SaveAdmittanceCarApplyReqDTO.class));
	}

	@Test
	public void applyRejectsProofBoundToAnotherMobileWithoutCallingSave() {
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		SmtAdmittanceApplyService applyService = Mockito.mock(SmtAdmittanceApplyService.class);
		VisitorTruckProofServiceImpl service = service(smsService, redisTemplate, applyService);
		SaveAdmittanceCarApplyReqDTO request = carApply("13900000000");
		Mockito.when(redisTemplate.execute(Mockito.any(), Mockito.eq(Collections.singletonList(
				"smart:admittance:visitor-truck:proof:proof-owner")), Mockito.eq("13900000000")))
				.thenReturn(null);

		expectSmartException(() -> service.apply("proof-owner", request));

		ArgumentCaptor<DefaultRedisScript> scriptCaptor = ArgumentCaptor.forClass(DefaultRedisScript.class);
		Mockito.verify(redisTemplate).execute(scriptCaptor.capture(), Mockito.eq(Collections.singletonList(
				"smart:admittance:visitor-truck:proof:proof-owner")), Mockito.eq("13900000000"));
		String script = scriptCaptor.getValue().getScriptAsString();
		Assert.assertTrue("Lua 必须先比对 proof 绑定手机号", script.contains("value == ARGV[1]"));
		Assert.assertTrue("Lua 只能在手机号相等分支删除 proof", script.contains("then redis.call('del', KEYS[1])"));
		Mockito.verify(applyService, Mockito.never()).saveAdmittanceCarApply(Mockito.any(SaveAdmittanceCarApplyReqDTO.class));
	}

	@Test
	public void applyConsumesProofExactlyOnceAndDelegatesWithNormalizedOwnerMobile() {
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		SmtAdmittanceApplyService applyService = Mockito.mock(SmtAdmittanceApplyService.class);
		VisitorTruckProofServiceImpl service = service(smsService, redisTemplate, applyService);
		SaveAdmittanceCarApplyReqDTO request = carApply("137 1234 1234");
		SmtAdmittanceApply saved = new SmtAdmittanceApply();
		Mockito.when(redisTemplate.execute(Mockito.any(), Mockito.eq(Collections.singletonList(
				"smart:admittance:visitor-truck:proof:proof-owner")), Mockito.eq("13712341234")))
				.thenReturn("13712341234", null);
		Mockito.when(applyService.saveAdmittanceCarApply(request)).thenReturn(saved);

		Assert.assertSame(saved, service.apply("proof-owner", request));
		expectSmartException(() -> service.apply("proof-owner", request));
		Assert.assertEquals("13712341234", request.getVisitorPhone());
		Mockito.verify(applyService).saveAdmittanceCarApply(request);
	}

	private VisitorTruckProofServiceImpl service(RemoteAppSmsService smsService, StringRedisTemplate redisTemplate,
			SmtAdmittanceApplyService applyService) {
		return new VisitorTruckProofServiceImpl(smsService, redisTemplate, applyService,
				(Supplier<String>) () -> "proof-fixed");
	}

	private RemoteAppSmsService visitorOnlySmsService(List<String> invokedMethods) {
		return (RemoteAppSmsService) Proxy.newProxyInstance(RemoteAppSmsService.class.getClassLoader(),
				new Class<?>[] {RemoteAppSmsService.class}, (proxy, method, arguments) -> {
					if ("verifyVisitorSmsCode".equals(method.getName())) {
						invokedMethods.add(method.getName());
						return Result.success(Boolean.TRUE);
					}
					if ("toString".equals(method.getName())) {
						return "visitor-only-sms-service";
					}
					return Result.success(Boolean.FALSE);
				});
	}

	private SaveAdmittanceCarApplyReqDTO carApply(String mobile) {
		SaveAdmittanceCarApplyReqDTO request = new SaveAdmittanceCarApplyReqDTO();
		request.setVisitorPhone(mobile);
		return request;
	}

	private void expectSmartException(ThrowingRunnable runnable) {
		try {
			runnable.run();
			Assert.fail("应拒绝无效货车预约 proof");
		} catch (SmartException expected) {
			// 预期拒绝无效或已消费的 proof。
		} catch (Exception error) {
			throw new AssertionError(error);
		}
	}

	@FunctionalInterface
	private interface ThrowingRunnable {
		void run() throws Exception;
	}
}
