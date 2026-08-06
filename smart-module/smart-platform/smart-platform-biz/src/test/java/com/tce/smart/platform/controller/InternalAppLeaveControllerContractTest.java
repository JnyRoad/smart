package com.tce.smart.platform.controller;

import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import org.junit.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/** App 离职新接口必须是 server scope 且保留 actor 形参，授权不能退回客户端 processId。 */
public class InternalAppLeaveControllerContractTest {
	@Test
	public void internalLeaveRoutesAreServerOnly() throws Exception {
		Class<?> controller = Class.forName("com.tce.smart.platform.controller.InternalAppLeaveController");
		assertServerRoute(controller, "save", PostMapping.class, "/application");
		assertServerRoute(controller, "yearHoliday", GetMapping.class, "/year-holiday");
		assertServerRoute(controller, "recordPage", GetMapping.class, "/record/page");
		assertServerRoute(controller, "application", GetMapping.class, "/application/{processId}");
		assertServerRoute(controller, "record", GetMapping.class, "/record/{processId}");
		assertServerRoute(controller, "handover", GetMapping.class, "/handover/{processId}");
		assertServerRoute(controller, "assignee", GetMapping.class, "/handover/assignee/{processId}");
		assertServerRoute(controller, "start", GetMapping.class, "/handover/start/{processId}");
		assertServerRoute(controller, "commit", PostMapping.class, "/handover/commit");
		assertServerRoute(controller, "close", GetMapping.class, "/handover/close/{processId}");
	}

	private void assertServerRoute(Class<?> controller, String methodName, Class<?> mapping, String expectedRoute) {
		Method method = null;
		for (Method candidate : controller.getMethods()) {
			if (methodName.equals(candidate.getName())) {
				method = candidate;
				break;
			}
		}
		assertNotNull(method);
		assertNotNull(method.getAnnotation(Inner.class));
		assertEquals("server", method.getAnnotation(OpenApi.class).value());
		if (GetMapping.class.equals(mapping)) {
			assertEquals(expectedRoute, method.getAnnotation(GetMapping.class).value()[0]);
		} else {
			assertEquals(expectedRoute, method.getAnnotation(PostMapping.class).value()[0]);
		}
	}
}
