package com.tce.smart.app.controller;

import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.feign.RemoteDormitoryService;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
	public void roomDetailRejectsAnonymousRequestBeforeFeignCall() {
		RemoteDormitoryService remoteService = Mockito.mock(RemoteDormitoryService.class);
		AppDormitoryController controller = new AppDormitoryController(remoteService);

		try {
			controller.getStaffRoomInfo("other-badge");
			fail("匿名请求不能被转发到内部住宿详情接口");
		} catch (AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(remoteService);
		}
	}

	@Test
	public void roomDetailRejectsBadgeDifferentFromAuthenticatedUserBeforeFeignCall() {
		RemoteDormitoryService remoteService = Mockito.mock(RemoteDormitoryService.class);
		AppDormitoryController controller = new AppDormitoryController(remoteService);
		authenticate("self-badge");

		try {
			controller.getStaffRoomInfo("other-badge");
			fail("客户端工号不能改变内部查询主体");
		} catch (AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(remoteService);
		}
	}

	private void authenticate(String badge) {
		SmartUser user = new SmartUser(1, 1, badge, Collections.singletonList(1), "N/A",
				true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "N/A", Collections.emptyList()));
	}
}
