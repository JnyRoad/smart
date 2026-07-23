package com.tce.smart.platform.controller;

import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.dto.req.SelfLockPwdRefreshReqDTO;
import com.tce.smart.platform.api.dto.req.SelfLockPwdUpdateReqDTO;
import com.tce.smart.platform.api.feign.RemoteSmartLockService;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * 门锁本人入口测试，避免客户端通过请求体或查询参数指定其他员工工号。
 */
public class SmtDormitoryStaffControllerSelfLockTest {

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void selfLockEndpointsUseAuthenticatedUsernameOnly() {
		SmtDormitoryStaffService service = Mockito.mock(SmtDormitoryStaffService.class);
		SmtDormitoryStaffController controller = new SmtDormitoryStaffController(
				service, Mockito.mock(RemoteSmartLockService.class));
		authenticate("self-badge");
		Mockito.when(service.getPwdForAuthenticatedStaff("self-badge")).thenReturn("cipher");
		Mockito.when(service.updateLockPwdForAuthenticatedStaff(Mockito.eq("self-badge"), Mockito.anyString()))
				.thenReturn("cipher");
		Mockito.when(service.refreshPwdForAuthenticatedStaff(Mockito.eq("self-badge"), Mockito.any(SelfLockPwdRefreshReqDTO.class)))
				.thenReturn("cipher");
		Mockito.when(service.faceCompareForAuthenticatedStaff(Mockito.eq("self-badge"), Mockito.any(SelfLockPwdRefreshReqDTO.class)))
				.thenReturn("cipher");

		assertEquals("cipher", controller.getPwdForCurrentUser().getData());
		SelfLockPwdUpdateReqDTO update = new SelfLockPwdUpdateReqDTO();
		update.setNewPwd("123456");
		assertEquals("cipher", controller.updateLockPwdForCurrentUser(update).getData());
		SelfLockPwdRefreshReqDTO refresh = new SelfLockPwdRefreshReqDTO();
		refresh.setFacePic("face-base64");
		assertEquals("cipher", controller.refreshPwdForCurrentUser(refresh).getData());
		assertEquals("cipher", controller.faceCompareForCurrentUser(refresh).getData());

		Mockito.verify(service).getPwdForAuthenticatedStaff("self-badge");
		Mockito.verify(service).updateLockPwdForAuthenticatedStaff("self-badge", "123456");
		Mockito.verify(service).refreshPwdForAuthenticatedStaff("self-badge", refresh);
		Mockito.verify(service).faceCompareForAuthenticatedStaff("self-badge", refresh);
	}

	@Test
	public void legacyFaceCompareHandlerIsNotPublicApiHandler() {
		for (java.lang.reflect.Method method : SmtDormitoryStaffController.class.getDeclaredMethods()) {
			if (!"faceCompare".equals(method.getName())) {
				continue;
			}
			org.junit.Assert.fail("旧的人脸比对入口不能继续由请求体指定员工工号");
		}
	}

	@Test
	public void selfLockEndpointsRejectAnonymousRequests() {
		SmtDormitoryStaffController controller = new SmtDormitoryStaffController(
				Mockito.mock(SmtDormitoryStaffService.class), Mockito.mock(RemoteSmartLockService.class));

		try {
			controller.getPwdForCurrentUser();
			fail("匿名请求不能读取门锁动态码");
		} catch (org.springframework.security.access.AccessDeniedException expected) {
			// 预期：认证缺失时不得把请求转交给按工号查询的服务。
		}
	}

	private void authenticate(String badge) {
		SmartUser user = new SmartUser(1, 1, badge, Collections.singletonList(1), "N/A",
				true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "N/A", Collections.emptyList()));
	}
}
