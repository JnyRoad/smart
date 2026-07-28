package com.tce.smart.platform.controller;

import com.tce.smart.algorithm.api.dto.req.FaceImgCutReq;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.service.ResumeFaceCropCapabilityService;
import com.tce.smart.platform.core.dto.AddJobFaceDTO;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CookieValue;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNull;

/**
 * 后台人脸裁剪必须经过平台鉴权代理，浏览器不得直连算法服务。
 */
public class FaceImageCropControllerContractTest {

	@Test
	public void cropRouteRequiresStaffManagementPermissionInsteadOfExposingAlgorithmRoute() throws Exception {
		Class<?> controllerType;
		try {
			controllerType = Class.forName("com.tce.smart.platform.controller.FaceImageCropController");
		} catch (ClassNotFoundException expected) {
			fail("必须提供经认证的后台人脸裁剪代理");
			return;
		}

		RequestMapping requestMapping = controllerType.getAnnotation(RequestMapping.class);
		assertNotNull(requestMapping);
		assertEquals("/face", requestMapping.value()[0]);

		Method crop = controllerType.getMethod("crop", FaceImgCutReq.class);
		assertEquals("/crop", crop.getAnnotation(PostMapping.class).value()[0]);
		assertEquals("@pms.hasPermission('platform_staff_manage')",
				crop.getAnnotation(PreAuthorize.class).value());
		if (crop.getAnnotation(Inner.class) != null) {
			fail("浏览器入口不得伪装成内部算法端点");
		}
	}

	@Test
	public void cropForwardsOnlyImageToInternalAlgorithmWithServiceTokenMarker() {
		RemoteAlgorithmService algorithmService = Mockito.mock(RemoteAlgorithmService.class);
		Mockito.when(algorithmService.cutFace(Mockito.any(FaceImgCutReq.class), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(Result.success("cropped-image"));
		FaceImageCropController controller = new FaceImageCropController(algorithmService);
		FaceImgCutReq request = new FaceImgCutReq();
		request.setSerialNo("untrusted-serial");
		request.setImageData("source-image");

		Result<String> response = controller.crop(request);

		ArgumentCaptor<FaceImgCutReq> requestCaptor = ArgumentCaptor.forClass(FaceImgCutReq.class);
		Mockito.verify(algorithmService).cutFace(requestCaptor.capture(), Mockito.eq(SecurityConstants.FROM_IN),
				Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		assertEquals("source-image", requestCaptor.getValue().getImageData());
		assertNotEquals("untrusted-serial", requestCaptor.getValue().getSerialNo());
		assertEquals("cropped-image", response.data());
	}

	@Test
	public void cropRejectsBlankImageWithoutCallingAlgorithm() {
		RemoteAlgorithmService algorithmService = Mockito.mock(RemoteAlgorithmService.class);
		FaceImageCropController controller = new FaceImageCropController(algorithmService);

		try {
			controller.crop(new FaceImgCutReq());
			fail("空图片不得调用算法服务");
		} catch (TCEException expected) {
			Mockito.verifyZeroInteractions(algorithmService);
		}
	}

	@Test
	public void publicResumeCropRouteRequiresSingleUseCookieCapability() throws Exception {
		Class<?> controllerType;
		try {
			controllerType = Class.forName("com.tce.smart.platform.controller.ResumeFaceCropController");
		} catch (ClassNotFoundException expected) {
			fail("公开简历流程必须具备受 capability 保护的人脸裁剪入口");
			return;
		}

		assertEquals("/regist/face", controllerType.getAnnotation(RequestMapping.class).value()[0]);
		Method crop = controllerType.getMethod("crop", String.class, FaceImgCutReq.class,
				javax.servlet.http.HttpServletResponse.class);
		assertEquals("/crop", crop.getAnnotation(PostMapping.class).value()[0]);
		assertEquals("resume_face_crop", crop.getParameters()[0].getAnnotation(CookieValue.class).value());
		assertNull("公开简历裁剪不能依赖后台管理员权限", crop.getAnnotation(PreAuthorize.class));
	}

	@Test
	public void resumeCropConsumesCapabilityAndBindsNextSaveCapabilityToCroppedImage() {
		RemoteAlgorithmService algorithmService = Mockito.mock(RemoteAlgorithmService.class);
		ResumeFaceCropCapabilityService capabilityService = Mockito.mock(ResumeFaceCropCapabilityService.class);
		javax.servlet.http.HttpServletResponse response = Mockito.mock(javax.servlet.http.HttpServletResponse.class);
		Mockito.when(capabilityService.consumeCropCapability("one-time-cookie")).thenReturn(73L);
		Mockito.when(algorithmService.cutFace(Mockito.any(FaceImgCutReq.class), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(Result.success("cropped-image"));
		ResumeFaceCropController controller = new ResumeFaceCropController(algorithmService, capabilityService);
		FaceImgCutReq request = new FaceImgCutReq();
		request.setImageData("source-image");

		Result<String> result = controller.crop("one-time-cookie", request, response);

		Mockito.verify(capabilityService).consumeCropCapability("one-time-cookie");
		Mockito.verify(capabilityService).issueSaveCapability(response, 73L, "cropped-image");
		Mockito.verify(algorithmService).cutFace(Mockito.any(FaceImgCutReq.class), Mockito.eq(SecurityConstants.FROM_IN),
				Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		assertEquals("cropped-image", result.data());
	}

	@Test
	public void resumeFaceSaveRequiresCapabilityBoundToTheCroppedImage() throws Exception {
		Method addFace = ResumeRegistController.class.getMethod("addFaceImg", String.class, AddJobFaceDTO.class);
		assertEquals("resume_face_save", addFace.getParameters()[0].getAnnotation(CookieValue.class).value());
	}
}
