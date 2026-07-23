package com.tce.smart.platform.controller;

import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.platform.api.dto.resp.InternalStaffBindingRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffIdentityRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffModuleRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffPasswordRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffProvisioningRespDTO;
import org.junit.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Platform 内部员工投影端点契约测试。
 */
public class InternalStaffControllerTest {

	@Test
	public void internalEndpointsAreExplicitlyMarkedAndUseDedicatedPaths() throws Exception {
		assertEquals("/internal/staff", InternalStaffController.class.getAnnotation(RequestMapping.class).value()[0]);
		assertInternalEndpoint("getBindingStaff", "/binding/{badge}", InternalStaffBindingRespDTO.class);
		assertInternalEndpoint("getModuleStaff", "/module/{badge}", InternalStaffModuleRespDTO.class);
		assertInternalEndpoint("getPasswordStaff", "/password/{badge}", InternalStaffPasswordRespDTO.class);
		assertInternalEndpoint("getIdentityStaff", "/ocr/{badge}", InternalStaffIdentityRespDTO.class);
		assertInternalEndpoint("getProvisioningStaff", "/provisioning/{badge}", InternalStaffProvisioningRespDTO.class);
	}

	private void assertInternalEndpoint(String methodName, String path, Class<?> expectedDataType) throws Exception {
		Method method = InternalStaffController.class.getMethod(methodName, String.class, String.class);
		GetMapping mapping = method.getAnnotation(GetMapping.class);
		RequestHeader fromHeader = method.getParameters()[1].getAnnotation(RequestHeader.class);
		assertNotNull(method.getAnnotation(Inner.class));
		OpenApi openApi = method.getAnnotation(OpenApi.class);
		assertNotNull(openApi);
		assertEquals("server", openApi.value());
		assertNotNull(fromHeader);
		assertEquals(path, mapping.value()[0]);
		assertEquals("Result", method.getReturnType().getSimpleName());
		assertNotNull("测试保留 DTO 类型引用，避免端点退化为员工实体", expectedDataType);
	}
}
