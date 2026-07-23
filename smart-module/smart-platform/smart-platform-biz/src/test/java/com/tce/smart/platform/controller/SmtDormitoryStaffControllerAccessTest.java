package com.tce.smart.platform.controller;

import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.feign.RemoteSmartLockService;
import com.tce.smart.platform.api.dto.resp.DormitoryRoomDetailRespDTO;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
	public void internalRoomDetailUsesDedicatedInnerRoute() throws Exception {
		Method method = SmtDormitoryStaffController.class.getMethod("getStaffRoomInfoForInternal", String.class);
		GetMapping mapping = method.getAnnotation(GetMapping.class);
		assertNotNull(method.getAnnotation(Inner.class));
		assertNotNull(method.getAnnotation(OpenApi.class));
		assertEquals("server", method.getAnnotation(OpenApi.class).value());
		assertEquals("/internal/roomDetail/{staffBadge}", mapping.value()[0]);
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
		return new SmtDormitoryStaffController(service, Mockito.mock(RemoteSmartLockService.class));
	}

	private void authenticate(String badge, Integer parkId) {
		SmartUser user = new SmartUser(1, 1, badge, Collections.singletonList(parkId), "N/A",
				true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "N/A", Collections.emptyList()));
	}
}
