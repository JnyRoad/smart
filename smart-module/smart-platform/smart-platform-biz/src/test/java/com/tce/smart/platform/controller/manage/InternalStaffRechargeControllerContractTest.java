package com.tce.smart.platform.controller.manage;

import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.platform.api.feign.manage.RemoteStaffRechargeService;
import org.junit.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/** 管理页保留原充值路径，定时任务必须走独立内部入口。 */
public class InternalStaffRechargeControllerContractTest {

	@Test
	public void scheduleRechargeUsesDedicatedServerOnlyContract() throws Exception {
		Class<?> controllerType = Class.forName("com.tce.smart.platform.controller.manage.InternalStaffRechargeController");
		Method senior = controllerType.getMethod("syncSenior", String.class, String.class);
		Method fresh = controllerType.getMethod("syncNew", String.class, String.class);
		assertNotNull(senior.getAnnotation(Inner.class));
		assertEquals("server", senior.getAnnotation(OpenApi.class).value());
		assertEquals("/senior", senior.getAnnotation(GetMapping.class).value()[0]);
		assertNotNull(fresh.getAnnotation(Inner.class));
		assertEquals("server", fresh.getAnnotation(OpenApi.class).value());

		Method feign = RemoteStaffRechargeService.class.getMethod("syncSeniorStaff", String.class, String.class, String.class);
		assertEquals("/internal/recharge/senior", feign.getAnnotation(GetMapping.class).value()[0]);
		assertEquals("X-Smart-Internal-Purpose", feign.getParameters()[2].getAnnotation(RequestHeader.class).value());
	}
}
