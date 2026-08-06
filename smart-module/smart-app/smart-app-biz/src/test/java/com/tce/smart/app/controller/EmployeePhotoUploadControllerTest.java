package com.tce.smart.app.controller;

import com.tce.smart.app.ao.wechat.CheckFaceAo;
import com.tce.smart.app.service.fore.VisitorService;
import com.tce.smart.app.vo.wechat.PhotoVisitorVo;
import com.tce.smart.algorithm.api.dto.req.FaceImgCutReq;
import com.tce.smart.common.security.service.SmartUser;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 门锁员工人脸接口必须保留受认证的 App 路由，不能回退到匿名访客入口。
 */
public class EmployeePhotoUploadControllerTest {

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void employeeFaceRoutesKeepTheirAuthenticatedAppMappings() throws Exception {
		assertRoute(EmployeeFaceCropController.class, "crop", "/employee/face", "/crop", FaceImgCutReq.class);
		assertRoute(EmployeePhotoUploadController.class, "upload", "/employee/photo", "/upload", CheckFaceAo.class);
	}

	@Test
	public void photoUploadRejectsAnonymousCallersAndAllowsTheAuthenticatedEmployee() {
		VisitorService visitorService = Mockito.mock(VisitorService.class);
		EmployeePhotoUploadController controller = new EmployeePhotoUploadController(visitorService);
		CheckFaceAo request = new CheckFaceAo();
		request.setVisitorPhoto("cut-base64");

		try {
			controller.upload(request);
			fail("匿名调用不得进入员工图片上传服务");
		} catch (org.springframework.security.access.AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(visitorService);
		}

		PhotoVisitorVo photo = new PhotoVisitorVo();
		Mockito.when(visitorService.checkFace(request, null, null)).thenReturn(photo);
		setAuthenticatedEmployee("employee-self");
		assertEquals(photo, controller.upload(request).data());
	}

	private void assertRoute(Class<?> controller, String methodName, String classPath, String methodPath,
			Class<?> parameterType) throws Exception {
		RequestMapping classMapping = controller.getAnnotation(RequestMapping.class);
		Method method = controller.getMethod(methodName, parameterType);
		PostMapping methodMapping = method.getAnnotation(PostMapping.class);
		assertTrue(Arrays.asList(classMapping.value()).contains(classPath));
		assertTrue(Arrays.asList(methodMapping.value()).contains(methodPath));
	}

	private void setAuthenticatedEmployee(String badge) {
		SmartUser user = new SmartUser(1, 1, badge, Collections.singletonList(5000021), "N/A", true, true,
				true, true, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "N/A", user.getAuthorities()));
	}
}
