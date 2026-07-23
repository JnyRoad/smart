package com.tce.smart.platform.controller.admittance;

import com.tce.smart.algorithm.api.dto.req.FaceImgCutReq;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.api.dto.req.admittance.VisitorFaceCropCapabilityReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.VisitorFaceCropReqDTO;
import com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorFaceCropRespDTO;
import com.tce.smart.platform.service.admittance.VisitorFaceCropCapabilityService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

/**
 * 网关精确放行的访客人脸路径必须依靠草稿会话和单次能力，而不能误标记为内部接口。
 */
public class VisitorFaceCropControllerTest {

	@Test
	public void routesAreExactAndNeverMarkedAsInternalTrustBypass() throws Exception {
		RequestMapping base = VisitorFaceCropController.class.getAnnotation(RequestMapping.class);
		assertEquals("/admittance/visitor-face", base.value()[0]);
		Method capability = VisitorFaceCropController.class.getMethod("issueCapability", String.class,
				VisitorFaceCropCapabilityReqDTO.class);
		Method crop = VisitorFaceCropController.class.getMethod("crop", String.class, VisitorFaceCropReqDTO.class);
		assertEquals("/capability", capability.getAnnotation(PostMapping.class).value()[0]);
		assertEquals("/crop", crop.getAnnotation(PostMapping.class).value()[0]);
		assertNull("访客浏览器入口不得伪装成内部服务调用", crop.getAnnotation(Inner.class));
	}

	@Test
	public void cropConsumesCapabilityBeforeCallingInternalAlgorithmAndRegeneratesSerial() {
		VisitorFaceCropCapabilityService capabilityService = Mockito.mock(VisitorFaceCropCapabilityService.class);
		RemoteAlgorithmService algorithmService = Mockito.mock(RemoteAlgorithmService.class);
		Mockito.when(algorithmService.cutFace(Mockito.any(FaceImgCutReq.class), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(Result.success("cropped-face"));
		VisitorFaceCropController controller = new VisitorFaceCropController(capabilityService, algorithmService);
		VisitorFaceCropReqDTO request = new VisitorFaceCropReqDTO();
		request.setDraftId("draft-1");
		request.setImageData("raw-face-image");

		Mockito.when(capabilityService.issueActionCapabilityForVerifiedDraft(Mockito.eq("draft-1"),
				Mockito.eq(VisitorActionCapabilityAction.FACE_UPLOAD), Mockito.anyString())).thenReturn("upload-capability");
		VisitorFaceCropRespDTO response = controller.crop("capability-1", request).data();
		assertEquals("cropped-face", response.getImageData());
		assertEquals("upload-capability", response.getUploadCapability());
		Mockito.verify(capabilityService).consumeCropCapability("capability-1", "draft-1");
		Mockito.verify(capabilityService).issueActionCapabilityForVerifiedDraft(Mockito.eq("draft-1"),
				Mockito.eq(VisitorActionCapabilityAction.FACE_UPLOAD), Mockito.anyString());
		ArgumentCaptor<FaceImgCutReq> capture = ArgumentCaptor.forClass(FaceImgCutReq.class);
		Mockito.verify(algorithmService).cutFace(capture.capture(), Mockito.eq(SecurityConstants.FROM_IN),
				Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		assertEquals("raw-face-image", capture.getValue().getImageData());
		assertNotEquals("draft-1", capture.getValue().getSerialNo());
	}
}
