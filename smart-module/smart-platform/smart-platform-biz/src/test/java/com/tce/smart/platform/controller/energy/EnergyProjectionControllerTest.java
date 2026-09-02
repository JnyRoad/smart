package com.tce.smart.platform.controller.energy;

import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.TestingAuthenticationToken;
import com.tce.smart.common.security.service.SmartUser;

import java.util.Arrays;
import java.util.Collections;
import java.time.LocalDate;
import com.tce.smart.platform.service.energy.EnergyProjectionService;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/** 园区能耗接口不允许空园区权限降级为全量读取。 */
public class EnergyProjectionControllerTest {
	@Test
	public void externalEndpointDoesNotAcceptDateOrPageArguments() throws Exception {
		Method method = EnergyProjectionController.class.getMethod("monthToDate", Long.class);
		assertEquals(1, method.getParameterTypes().length);
		assertEquals("/sd/statistics/month/{parkId}", method.getAnnotation(GetMapping.class).value()[0]);
		assertEquals("@pms.hasPermission('platform_energy_usage_view')", method.getAnnotation(PreAuthorize.class).value());
	}

	@Test
	public void missingUserFailsClosed() {
		try { EnergyProjectionController.assertParkAuthorized(null, 1L); fail("空登录态必须拒绝"); }
		catch (AccessDeniedException expected) { assertEquals("无权访问该园区能耗数据", expected.getMessage()); }
	}

	@Test
	public void internalProjectionEndpointsRequireDedicatedProjectionScope() throws Exception {
		assertDedicatedProjectionScope("processPending");
		assertDedicatedProjectionScope("reconcile", String.class);
		assertDedicatedProjectionScope("backfillMonthToDate");
		assertDedicatedProjectionScope("daily", String.class, boolean.class, boolean.class);
	}

	@Test
	public void dailyOrchestrationEndpointIsSingleInternalCall() throws Exception {
		Method method = EnergyProjectionController.class.getMethod("daily", String.class, boolean.class, boolean.class);
		assertEquals("/inner/energy/projection/daily/{businessDate}", method.getAnnotation(PostMapping.class).value()[0]);
		assertEquals(3, method.getParameterTypes().length);
	}

	@Test
	public void dailyRejectsInvalidFlagsAndDateThenRunsReconcileBeforeBackfill() {
		EnergyProjectionService service = mock(EnergyProjectionService.class);
		EnergyProjectionController controller = new EnergyProjectionController(service);
		try { controller.daily("invalid", false, false); fail("双 false 必须拒绝"); } catch (IllegalArgumentException expected) { }
		try { controller.daily("invalid", false, true); fail("仅回填也必须校验业务日期"); } catch (java.time.format.DateTimeParseException expected) { }
		controller.daily("2026-08-05", true, true);
		InOrder order = inOrder(service);
		order.verify(service).reconcile(LocalDate.of(2026, 8, 5));
		order.verify(service).backfillCurrentMonthToDate();
	}

	@Test
	public void unauthenticatedAndWrongPrincipalFailClosed() {
		TestingAuthenticationToken wrongPrincipal = new TestingAuthenticationToken("not-smart-user", "x"); wrongPrincipal.setAuthenticated(true);
		try { EnergyProjectionController.assertAuthenticationCanReadPark(wrongPrincipal, 1L); fail("错误主体必须拒绝"); } catch (AccessDeniedException expected) { }
		TestingAuthenticationToken unauthenticated = new TestingAuthenticationToken(user(Collections.singletonList(1)), "x");
		unauthenticated.setAuthenticated(false);
		try { EnergyProjectionController.assertAuthenticationCanReadPark(unauthenticated, 1L); fail("未认证用户必须拒绝"); } catch (AccessDeniedException expected) { }
		try { EnergyProjectionController.assertParkAuthorized(user(Collections.<Integer>emptyList()), 1L); fail("空园区必须拒绝"); } catch (AccessDeniedException expected) { }
	}

	private SmartUser user(java.util.List<Integer> parks) {
		return new SmartUser(1, 1, "test", parks, "x", true, true, true, true, Collections.emptyList());
	}

	/**
	 * 所有能耗投影内部入口使用同一个最小能力 scope，并在滚动升级期精确兼容旧 server scope。
	 */
	private void assertDedicatedProjectionScope(String methodName, Class<?>... parameterTypes) throws Exception {
		Method method = EnergyProjectionController.class.getMethod(methodName, parameterTypes);
		OpenApi openApi = method.getAnnotation(OpenApi.class);
		assertEquals("internal:energy:projection:run", openApi.value());
		assertArrayEquals(new String[] {"server"}, openApi.compatibilityScopes());
		assertEquals(Inner.class, method.getAnnotation(Inner.class).annotationType());
	}
}
