package com.tce.smart.platform.controller;

import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.api.dto.resp.InternalStaffBindingRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffIdentityRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffModuleRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffPasswordRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffProvisioningRespDTO;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

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
		assertIdentityInternalEndpoint();
		assertInternalEndpoint("getProvisioningStaff", "/provisioning/{badge}", InternalStaffProvisioningRespDTO.class);
	}

	@Test
	public void identityLookupRequiresApprovedClientAndExplicitPurpose() {
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		OpenApiAuthenticationAdapter adapter = Mockito.mock(OpenApiAuthenticationAdapter.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		Mockito.when(adapter.isClientOnly(authentication)).thenReturn(true);
		Mockito.when(adapter.clientId(authentication)).thenReturn("app");
		SmtStaff staff = new SmtStaff();
		staff.setBadge("self-badge");
		staff.setCertno("123456789012345678");
		Mockito.when(staffService.getSimpleSttaffByBadge("self-badge")).thenReturn(staff);

		InternalStaffController controller = new InternalStaffController(staffService, adapter);
		assertEquals("123456789012345678", controller.getIdentityStaff("self-badge", SecurityConstants.FROM_IN, "ocr-compare").getData().getCertno());

		try {
			controller.getIdentityStaff("self-badge", SecurityConstants.FROM_IN, "unapproved-purpose");
			fail("身份资料用途不在白名单时必须拒绝");
		} catch (AccessDeniedException expected) {
			// 预期：用途白名单拒绝。
		}

		Mockito.when(adapter.clientId(authentication)).thenReturn("other-service");
		try {
			controller.getIdentityStaff("self-badge", SecurityConstants.FROM_IN, "icbc-eaccount");
			fail("任意 server token 不得读取身份资料");
		} catch (AccessDeniedException expected) {
			// 预期：调用服务白名单拒绝。
		}
		SecurityContextHolder.clearContext();
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

	private void assertIdentityInternalEndpoint() throws Exception {
		Method method = InternalStaffController.class.getMethod("getIdentityStaff", String.class, String.class, String.class);
		GetMapping mapping = method.getAnnotation(GetMapping.class);
		assertNotNull(method.getAnnotation(Inner.class));
		assertEquals("server", method.getAnnotation(OpenApi.class).value());
		assertEquals("/ocr/{badge}", mapping.value()[0]);
		assertNotNull(InternalStaffIdentityRespDTO.class);
	}

}
