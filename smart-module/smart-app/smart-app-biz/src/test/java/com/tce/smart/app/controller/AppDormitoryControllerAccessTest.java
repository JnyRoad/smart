package com.tce.smart.app.controller;

import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.platform.api.feign.RemoteDormitoryService;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.Assert.fail;

/**
 * App 宿舍接口访问控制测试，防止客户端路径工号被转发到内部 Feign 接口。
 */
public class AppDormitoryControllerAccessTest {

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void myRoomDetailRejectsAnonymousRequestBeforeFeignCall() {
		RemoteDormitoryService remoteService = Mockito.mock(RemoteDormitoryService.class);
		AppDormitoryController controller = new AppDormitoryController(remoteService);

		try {
			controller.getMyRoomDetail();
			fail("匿名请求不能被转发到内部住宿详情接口");
		} catch (AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(remoteService);
		}
	}

	@Test
	public void myRoomDetailUsesOnlyAuthenticatedSubjectAndServiceTokenContract() {
		RemoteDormitoryService remoteService = Mockito.mock(RemoteDormitoryService.class);
		AppDormitoryController controller = new AppDormitoryController(remoteService);
		setPrivateField(controller, "appRoomPurpose", "app-self-room");
		authenticate("self-badge");

		controller.getMyRoomDetail();

		Mockito.verify(remoteService).getSelfRoomDetail("self-badge", SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED, "app-self-room");
		Mockito.verify(remoteService, Mockito.never()).getStaffRoomInfo(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString());
	}

	@Test
	public void myRoomListUsesOnlyAuthenticatedSubjectAndServiceTokenContract() {
		RemoteDormitoryService remoteService = Mockito.mock(RemoteDormitoryService.class);
		AppDormitoryController controller = new AppDormitoryController(remoteService);
		setPrivateField(controller, "appRoomPurpose", "app-self-room");
		authenticate("self-badge");

		controller.getMyRoomList();

		Mockito.verify(remoteService).getStaffRoomInfoList("self-badge", SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED, "app-self-room");
	}

	@Test
	public void myRoomEndpointsRejectMissingManagedPurposeBeforeFeignCall() {
		RemoteDormitoryService remoteService = Mockito.mock(RemoteDormitoryService.class);
		AppDormitoryController controller = new AppDormitoryController(remoteService);
		authenticate("self-badge");

		try {
			controller.getMyRoomDetail();
			fail("用途配置缺失时不得发起内部住宿详情调用");
		} catch (AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(remoteService);
		}
	}

	@Test
	public void legacyBadgePathMethodsAreNotExposedByAppController() {
		try {
			AppDormitoryController.class.getMethod("getStaffRoomInfo", String.class);
			fail("App 不得再暴露路径工号住宿详情接口");
		} catch (NoSuchMethodException expected) {
			// 预期：客户端只能由认证主体确定员工工号。
		}
		try {
			AppDormitoryController.class.getMethod("getStaffRoomInfoList", String.class);
			fail("App 不得再暴露路径工号住宿列表接口");
		} catch (NoSuchMethodException expected) {
			// 预期：客户端只能读取本人的入住记录。
		}
	}

	private void authenticate(String badge) {
		SmartUser user = new SmartUser(1, 1, badge, Collections.singletonList(1), "N/A",
				true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "N/A", Collections.emptyList()));
	}

	private void setPrivateField(Object target, String fieldName, String value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}
}
