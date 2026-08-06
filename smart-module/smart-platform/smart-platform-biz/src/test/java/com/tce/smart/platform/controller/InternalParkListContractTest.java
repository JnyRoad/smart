package com.tce.smart.platform.controller;

import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.platform.api.feign.RemoteParkInternalService;
import org.junit.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/** 外部园区目录保留给 UI，服务调用必须迁移至专用内部列表。 */
public class InternalParkListContractTest {

	@Test
	public void fullParkListUsesDedicatedServerOnlyContract() throws Exception {
		Class<?> controllerType = Class.forName("com.tce.smart.platform.controller.InternalParkController");
		Method endpoint = controllerType.getMethod("getAllParks", String.class, String.class);
		assertNotNull(endpoint.getAnnotation(Inner.class));
		assertEquals("server", endpoint.getAnnotation(OpenApi.class).value());
		assertEquals("/all", endpoint.getAnnotation(GetMapping.class).value()[0]);

		Method feign = RemoteParkInternalService.class.getMethod("getAllParks", String.class, String.class, String.class);
		assertEquals("/internal/park/all", feign.getAnnotation(GetMapping.class).value()[0]);
		assertEquals("X-Smart-Internal-Purpose", feign.getParameters()[2].getAnnotation(RequestHeader.class).value());
	}
}
