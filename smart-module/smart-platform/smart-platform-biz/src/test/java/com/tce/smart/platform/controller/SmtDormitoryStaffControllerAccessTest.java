package com.tce.smart.platform.controller;

import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.platform.api.feign.RemoteSmartLockService;
import com.tce.smart.platform.api.dto.resp.DormitoryRoomDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.SelfDormitoryRoomRespDTO;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 住宿详情的外部园区隔离与内部 Feign 路由契约测试。
 */
public class SmtDormitoryStaffControllerAccessTest {

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void externalRoomDetailRejectsAnonymousRequest() {
		try {
			controller(Mockito.mock(SmtDormitoryStaffService.class)).getStaffRoomInfoForAdmin("staff-badge");
			fail("匿名请求不能读取员工住宿位置");
		} catch (AccessDeniedException expected) {
			// 预期：认证缺失时拒绝外部详情查询。
		}
	}

	@Test
	public void externalRoomDetailRejectsStaffOutsideCurrentUsersParks() {
		SmtDormitoryStaffService service = Mockito.mock(SmtDormitoryStaffService.class);
		DormitoryRoomDetailRespDTO response = DormitoryRoomDetailRespDTO.builder().parkId(2).build();
		Mockito.when(service.getStaffRoomInfo("staff-badge")).thenReturn(response);
		authenticate("admin", 1);

		try {
			controller(service).getStaffRoomInfoForAdmin("staff-badge");
			fail("跨园区管理员不能读取员工住宿位置");
		} catch (AccessDeniedException expected) {
			// 预期：响应中的园区不在当前用户数据范围内。
		}
	}

	@Test
	public void internalSelfRoomDetailUsesDedicatedMinimalInnerRoute() throws Exception {
		Method method = SmtDormitoryStaffController.class.getMethod("getSelfRoomDetailForInternal", String.class, String.class,
				String.class);
		GetMapping mapping = method.getAnnotation(GetMapping.class);
		assertNotNull(method.getAnnotation(Inner.class));
		assertNotNull(method.getAnnotation(OpenApi.class));
		assertEquals("server", method.getAnnotation(OpenApi.class).value());
		assertEquals("/internal/self/roomDetail/{staffBadge}", mapping.value()[0]);
	}

	@Test
	public void internalRoomListUsesDedicatedServiceTokenRoute() throws Exception {
		Method method = SmtDormitoryStaffController.class.getMethod("getStaffRoomInfoListForInternal", String.class, String.class, String.class);
		GetMapping mapping = method.getAnnotation(GetMapping.class);
		assertNotNull(method.getAnnotation(Inner.class));
		assertNotNull(method.getAnnotation(OpenApi.class));
		assertEquals("server", method.getAnnotation(OpenApi.class).value());
		assertEquals("/internal/roomList/{staffBadge}", mapping.value()[0]);
	}

	@Test
	public void internalRoomListRejectsGenericServerClientAndMissingClientConfiguration() throws Exception {
		SmtDormitoryStaffService service = Mockito.mock(SmtDormitoryStaffService.class);
		OpenApiAuthenticationAdapter adapter = Mockito.mock(OpenApiAuthenticationAdapter.class);
		org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		Mockito.when(adapter.isClientOnly(authentication)).thenReturn(true);
		Mockito.when(adapter.clientId(authentication)).thenReturn("app");

		SmtDormitoryStaffController controller = controller(service, adapter);
		try {
			controller.getStaffRoomInfoListForInternal("staff-badge", SecurityConstants.FROM_IN, "configured-room-purpose");
			fail("受管 App client_id 配置缺失时不得读取员工住宿列表");
		} catch (AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(service);
		}

		setPrivateField(controller, "appServiceClientId", "app");
		setPrivateField(controller, "appRoomPurpose", "configured-room-purpose");
		try {
			controller.getStaffRoomInfoListForInternal("staff-badge", SecurityConstants.FROM_IN, "other-purpose");
			fail("未经审核的内部用途不得读取员工住宿列表");
		} catch (AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(service);
		}

		Mockito.when(adapter.clientId(authentication)).thenReturn("generic-server-client");
		try {
			controller.getStaffRoomInfoListForInternal("staff-badge", SecurityConstants.FROM_IN, "configured-room-purpose");
			fail("非受管 App client_id 不得读取员工住宿列表");
		} catch (AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(service);
		}
	}

	@Test
	public void internalRoomListReturnsMinimalResponseWithoutBadgeOrDynamicPassword() throws Exception {
		SmtDormitoryStaffService service = Mockito.mock(SmtDormitoryStaffService.class);
		OpenApiAuthenticationAdapter adapter = Mockito.mock(OpenApiAuthenticationAdapter.class);
		org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		Mockito.when(adapter.isClientOnly(authentication)).thenReturn(true);
		Mockito.when(adapter.clientId(authentication)).thenReturn("app");
		DormitoryRoomDetailRespDTO room = DormitoryRoomDetailRespDTO.builder()
				.id(1).parkId(2).dormitoryId(3).dormitoryName("宿舍").floorId(4).floorName("二层")
				.roomId(5).roomName("201").bedNumber("1").staffBadge("staff-badge").build();
		Mockito.when(service.getStaffRoomInfoList("staff-badge")).thenReturn(Collections.singletonList(room));
		SmtDormitoryStaffController controller = controller(service, adapter);
		setPrivateField(controller, "appServiceClientId", "app");
		setPrivateField(controller, "appRoomPurpose", "configured-room-purpose");

		Object response = controller.getStaffRoomInfoListForInternal("staff-badge", SecurityConstants.FROM_IN,
				"configured-room-purpose").getData().get(0);
		assertEquals(SelfDormitoryRoomRespDTO.class, response.getClass());
		String json = new ObjectMapper().writeValueAsString(response);
		assertFalse(json.contains("staffBadge"));
		assertFalse(json.contains("lockPwd"));
		assertFalse(json.contains("dynamicDesc"));
		Set<String> fields = Arrays.stream(SelfDormitoryRoomRespDTO.class.getDeclaredFields())
				.filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
				.map(Field::getName).collect(Collectors.toSet());
		assertEquals(new java.util.HashSet<>(Arrays.asList("id", "bedNumber", "parkId", "parkName", "dormitoryId",
				"dormitoryName", "floorId", "floorName", "roomId", "roomName", "inRecordId")), fields);
	}

	@Test
	public void internalSelfRoomDetailReturnsMinimalResponseWithoutSensitiveFields() throws Exception {
		SmtDormitoryStaffService service = Mockito.mock(SmtDormitoryStaffService.class);
		OpenApiAuthenticationAdapter adapter = Mockito.mock(OpenApiAuthenticationAdapter.class);
		org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		Mockito.when(adapter.isClientOnly(authentication)).thenReturn(true);
		Mockito.when(adapter.clientId(authentication)).thenReturn("app");
		DormitoryRoomDetailRespDTO room = DormitoryRoomDetailRespDTO.builder().id(1).parkId(2).dormitoryId(3)
				.dormitoryName("宿舍").floorId(4).floorName("二层").roomId(5).roomName("201")
				.bedNumber("1").staffBadge("staff-badge").staffName("员工").depName("部门").jobName("岗位").build();
		Mockito.when(service.getStaffRoomInfo("staff-badge")).thenReturn(room);
		SmtDormitoryStaffController controller = controller(service, adapter);
		setPrivateField(controller, "appServiceClientId", "app");
		setPrivateField(controller, "appRoomPurpose", "configured-room-purpose");

		Object response = controller.getSelfRoomDetailForInternal("staff-badge", SecurityConstants.FROM_IN,
				"configured-room-purpose").getData();
		assertEquals(SelfDormitoryRoomRespDTO.class, response.getClass());
		String json = new ObjectMapper().writeValueAsString(response);
		assertFalse(json.contains("staffBadge"));
		assertFalse(json.contains("staffName"));
		assertFalse(json.contains("depName"));
		assertFalse(json.contains("jobName"));
		assertFalse(json.contains("lockPwd"));
		assertFalse(json.contains("dynamicDesc"));
	}

	@Test
	public void currentUserRoomListReturnsMinimalResponseWithoutSensitiveFields() throws Exception {
		SmtDormitoryStaffService service = Mockito.mock(SmtDormitoryStaffService.class);
		DormitoryRoomDetailRespDTO room = DormitoryRoomDetailRespDTO.builder().id(1).parkId(2).dormitoryId(3)
				.dormitoryName("宿舍").floorId(4).floorName("二层").roomId(5).roomName("201")
				.bedNumber("1").staffBadge("self-badge").staffName("员工").depName("部门").jobName("岗位").build();
		Mockito.when(service.getStaffRoomInfoList("self-badge")).thenReturn(Collections.singletonList(room));
		authenticate("self-badge", 2);

		Object response = controller(service).getRoomListForCurrentUser().getData().get(0);
		assertEquals(SelfDormitoryRoomRespDTO.class, response.getClass());
		String json = new ObjectMapper().writeValueAsString(response);
		assertFalse(json.contains("staffBadge"));
		assertFalse(json.contains("staffName"));
		assertFalse(json.contains("depName"));
		assertFalse(json.contains("jobName"));
		assertFalse(json.contains("lockPwd"));
		assertFalse(json.contains("dynamicDesc"));
	}

	@Test
	public void internalFullRoomDetailRejectsUnmanagedClient() {
		SmtDormitoryStaffService service = Mockito.mock(SmtDormitoryStaffService.class);
		OpenApiAuthenticationAdapter adapter = Mockito.mock(OpenApiAuthenticationAdapter.class);
		org.springframework.security.core.Authentication authentication = Mockito.mock(org.springframework.security.core.Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		Mockito.when(adapter.isClientOnly(authentication)).thenReturn(true);
		Mockito.when(adapter.clientId(authentication)).thenReturn("generic-server-client");

		try {
			controller(service, adapter).getStaffRoomInfoForInternal("staff-badge", SecurityConstants.FROM_IN,
					"unmanaged-purpose");
			fail("未受管客户端不得读取完整住宿详情");
		} catch (AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(service);
		}
	}

	@Test
	public void legacyPhoneAndNameRoomLookupIsNotAnExternalControllerMethod() {
		try {
			SmtDormitoryStaffController.class.getMethod("getStaffRoomInfoByPhone", String.class, String.class);
			fail("手机号和姓名不能作为住宿信息查询凭据");
		} catch (NoSuchMethodException expected) {
			// 预期：旧的手机号加姓名查询路由已删除。
		}
	}

	private SmtDormitoryStaffController controller(SmtDormitoryStaffService service) {
		return controller(service, Mockito.mock(OpenApiAuthenticationAdapter.class));
	}

	private SmtDormitoryStaffController controller(SmtDormitoryStaffService service, OpenApiAuthenticationAdapter adapter) {
		return new SmtDormitoryStaffController(service, Mockito.mock(RemoteSmartLockService.class), adapter);
	}

	private void setPrivateField(Object target, String fieldName, String value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}

	private void authenticate(String badge, Integer parkId) {
		SmartUser user = new SmartUser(1, 1, badge, Collections.singletonList(parkId), "N/A",
				true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "N/A", Collections.emptyList()));
	}
}
