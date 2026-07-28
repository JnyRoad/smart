package com.tce.smart.app.controller;

import com.tce.smart.algorithm.api.dto.req.FaceImgCutReq;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.service.SmartUser;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

/**
 * 员工人脸裁剪只允许当前认证主体，且不接受客户端伪造内部序列号。
 */
public class EmployeeFaceCropControllerTest {

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void authenticatedEmployeeCanCropWithoutSupplyingAnyOtherIdentity() {
		RemoteAlgorithmService algorithmService = Mockito.mock(RemoteAlgorithmService.class);
		Mockito.when(algorithmService.cutFace(Mockito.any(FaceImgCutReq.class), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(Result.success("cropped-face"));
		EmployeeFaceCropController controller = new EmployeeFaceCropController(algorithmService);
		setAuthenticatedUser("employee-001");
		FaceImgCutReq request = new FaceImgCutReq();
		request.setSerialNo("untrusted-serial");
		request.setImageData("raw-face-image");

		assertEquals("cropped-face", controller.crop(request).data());
		ArgumentCaptor<FaceImgCutReq> capture = ArgumentCaptor.forClass(FaceImgCutReq.class);
		Mockito.verify(algorithmService).cutFace(capture.capture(), Mockito.eq(SecurityConstants.FROM_IN),
				Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		assertEquals("raw-face-image", capture.getValue().getImageData());
		assertNotEquals("untrusted-serial", capture.getValue().getSerialNo());
	}

	@Test
	public void anonymousCallerCannotInvokeAlgorithm() {
		RemoteAlgorithmService algorithmService = Mockito.mock(RemoteAlgorithmService.class);
		EmployeeFaceCropController controller = new EmployeeFaceCropController(algorithmService);
		FaceImgCutReq request = new FaceImgCutReq();
		request.setImageData("raw-face-image");

		try {
			controller.crop(request);
			fail("匿名调用不得进入内部算法");
		} catch (org.springframework.security.access.AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(algorithmService);
		}
	}

	private void setAuthenticatedUser(String badge) {
		SmartUser user = new SmartUser(1, 1, badge, Collections.singletonList(5000021), "N/A", true, true,
				true, true, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, "N/A", user.getAuthorities()));
	}
}
