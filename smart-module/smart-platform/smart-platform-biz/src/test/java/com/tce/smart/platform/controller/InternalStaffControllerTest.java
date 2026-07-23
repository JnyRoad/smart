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
import com.tce.smart.platform.api.dto.resp.InternalScheduleIscPersonRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalScheduleStaffIdentityRespDTO;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Platform 内部员工投影端点契约测试。
 */
public class InternalStaffControllerTest {

	@Test
	public void internalEndpointsAreExplicitlyMarkedAndUseDedicatedPaths() throws Exception {
		assertEquals("/internal/staff", InternalStaffController.class.getAnnotation(RequestMapping.class).value()[0]);
		assertInternalEndpoint("getBindingStaff", "/binding/{badge}", InternalStaffBindingRespDTO.class,
				String.class, String.class);
		assertInternalEndpoint("getModuleStaff", "/module/{badge}", InternalStaffModuleRespDTO.class,
				String.class, String.class);
		assertInternalEndpoint("getPasswordStaff", "/password/{badge}", InternalStaffPasswordRespDTO.class,
				String.class, String.class, String.class);
		assertIdentityInternalEndpoint();
		assertInternalEndpoint("getProvisioningStaff", "/provisioning/{badge}", InternalStaffProvisioningRespDTO.class,
				String.class, String.class, String.class);
		assertScheduleInternalEndpoint("getScheduleIscPersonStaff", "/schedule/isc-person/{staffId}", InternalScheduleIscPersonRespDTO.class);
		assertScheduleInternalEndpoint("getScheduleIdentityStaff", "/schedule/identity/{staffId}", InternalScheduleStaffIdentityRespDTO.class);
	}

	@Test
	public void scheduleEndpointsRequireClientCredentialsAndReturnOnlyPurposeFields() {
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		OpenApiAuthenticationAdapter adapter = Mockito.mock(OpenApiAuthenticationAdapter.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		SmtStaff staff = new SmtStaff();
		staff.setId(100L);
		staff.setBadge("schedule-badge");
		staff.setName("排程人员");
		staff.setSex(1);
		staff.setBirth("19900101");
		staff.setCertno("123456789012345678");
		staff.setStatus(1);
		staff.setPhone("13800138000");
		staff.setEmail("schedule@example.invalid");
		Mockito.when(staffService.getSimpleSttaffById("100")).thenReturn(staff);
		InternalStaffController controller = new InternalStaffController(staffService, adapter);
		setPrivateField(controller, "scheduleServiceClientId", "smart-schedule");

		try {
			controller.getScheduleIscPersonStaff("100", SecurityConstants.FROM_IN);
			fail("仅伪造 from=Y 不能读取 Schedule 身份资料");
		} catch (AccessDeniedException expected) {
			// 预期：必须存在服务 client_credentials 认证主体。
		}

		Mockito.when(adapter.isClientOnly(authentication)).thenReturn(true);
		Mockito.when(adapter.clientId(authentication)).thenReturn("other-service");
		try {
			controller.getScheduleIscPersonStaff("100", SecurityConstants.FROM_IN);
			fail("非 Schedule 服务令牌不能读取 Schedule 身份资料");
		} catch (AccessDeniedException expected) {
			// 预期：服务令牌 client_id 必须在 Schedule 专用白名单内。
		}

		Mockito.when(adapter.clientId(authentication)).thenReturn("smart-schedule");
		InternalScheduleIscPersonRespDTO iscPerson = controller
				.getScheduleIscPersonStaff("100", SecurityConstants.FROM_IN).getData();
		InternalScheduleStaffIdentityRespDTO identity = controller
				.getScheduleIdentityStaff("100", SecurityConstants.FROM_IN).getData();
		assertEquals("schedule-badge", iscPerson.getBadge());
		assertEquals("排程人员", iscPerson.getName());
		assertEquals("19900101", iscPerson.getBirth());
		assertEquals("123456789012345678", iscPerson.getCertno());
		assertEquals("schedule-badge", identity.getBadge());
		assertEquals("123456789012345678", identity.getCertno());
		assertEquals(Integer.valueOf(1), identity.getStatus());
		assertExactFields(InternalScheduleIscPersonRespDTO.class, "badge", "name", "sex", "birth", "certno");
		assertExactFields(InternalScheduleStaffIdentityRespDTO.class, "badge", "certno", "status");
		SecurityContextHolder.clearContext();
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

	@Test
	public void sensitiveInternalLookupsRequireClientAllowlistAndPurpose() throws Exception {
		String source = new String(Files.readAllBytes(Paths.get(
				"src/main/java/com/tce/smart/platform/controller/InternalStaffController.java")), StandardCharsets.UTF_8);
		assertTrue("密码手机号端点必须声明用途", source.contains("getPasswordPhone")
				&& source.contains("PASSWORD_PHONE_PURPOSES"));
		assertTrue("本人资料端点必须声明用途", source.contains("getSelfProfile")
				&& source.contains("SELF_PROFILE_PURPOSES"));
		assertTrue("账号开通端点必须声明用途", source.contains("getProvisioningStaff")
				&& source.contains("PROVISIONING_PURPOSES"));
		assertTrue("高敏感端点必须复用客户端白名单校验", source.contains("assertCallerAndPurpose"));
	}

	@Test
	public void passwordPhoneRejectsWrongPurposeAndNonAppClient() {
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		OpenApiAuthenticationAdapter adapter = Mockito.mock(OpenApiAuthenticationAdapter.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		Mockito.when(adapter.isClientOnly(authentication)).thenReturn(true);
		Mockito.when(adapter.clientId(authentication)).thenReturn("app");
		InternalStaffController controller = new InternalStaffController(staffService, adapter);

		try {
			controller.getPasswordPhone("any-badge", SecurityConstants.FROM_IN, "self-profile");
			fail("错误用途不得读取密码找回手机号");
		} catch (AccessDeniedException expected) {
			// 预期：端点级用途白名单拒绝。
		}

		Mockito.when(adapter.clientId(authentication)).thenReturn("another-server-client");
		try {
			controller.getPasswordPhone("any-badge", SecurityConstants.FROM_IN, "password-reset");
			fail("任意 server client 不得按工号读取密码找回手机号");
		} catch (AccessDeniedException expected) {
			// 预期：客户端白名单拒绝。
		}
		SecurityContextHolder.clearContext();
	}

	private void assertInternalEndpoint(String methodName, String path, Class<?> expectedDataType,
			Class<?>... parameterTypes) throws Exception {
		Method method = InternalStaffController.class.getMethod(methodName, parameterTypes);
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

	private void assertScheduleInternalEndpoint(String methodName, String path, Class<?> expectedDataType) throws Exception {
		Method method = InternalStaffController.class.getMethod(methodName, String.class, String.class);
		GetMapping mapping = method.getAnnotation(GetMapping.class);
		assertNotNull(method.getAnnotation(Inner.class));
		assertEquals("server", method.getAnnotation(OpenApi.class).value());
		assertEquals(path, mapping.value()[0]);
		assertEquals("Result", method.getReturnType().getSimpleName());
		assertNotNull("Schedule 路由必须返回最小 DTO，不得退化为员工实体", expectedDataType);
	}

	private void assertExactFields(Class<?> dtoType, String... expectedFields) {
		List<String> actualFields = Arrays.stream(dtoType.getDeclaredFields())
				.map(field -> field.getName())
				.collect(Collectors.toList());
		assertEquals(Arrays.asList(expectedFields), actualFields);
	}

	private void setPrivateField(Object target, String fieldName, String value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("内部服务客户端配置字段缺失", e);
		}
	}

}
