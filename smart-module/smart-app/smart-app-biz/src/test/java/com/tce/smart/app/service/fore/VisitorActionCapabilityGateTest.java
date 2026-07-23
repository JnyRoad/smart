package com.tce.smart.app.service.fore;

import com.tce.smart.app.ao.wechat.CheckFaceAo;
import com.tce.smart.app.service.fore.impl.VisitorServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction;
import com.tce.smart.platform.api.dto.req.admittance.VisitorActionCapabilityConsumeReqDTO;
import com.tce.smart.platform.api.dto.req.SaveImageReqDto;
import com.tce.smart.platform.api.feign.RemoteSmtImageService;
import com.tce.smart.platform.api.feign.RemoteVisitorService;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/** 匿名访客上传必须先在 Platform 原子消费与图片摘要绑定的 capability。 */
public class VisitorActionCapabilityGateTest {

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void anonymousUploadWithoutCapabilityFailsBeforeImageStorage() {
		RemoteVisitorService visitorRemote = Mockito.mock(RemoteVisitorService.class);
		RemoteSmtImageService imageRemote = Mockito.mock(RemoteSmtImageService.class);
		VisitorServiceImpl service = service(visitorRemote, imageRemote);
		CheckFaceAo request = new CheckFaceAo();
		request.setVisitorPhoto("base64-image");

		try {
			service.checkFace(request, null, null);
			fail("匿名调用不得进入图片存储");
		} catch (org.springframework.security.access.AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(visitorRemote, imageRemote);
		}
	}

	@Test
	public void validCapabilityIsConsumedWithServiceCredentialsBeforeImageStorage() {
		RemoteVisitorService visitorRemote = Mockito.mock(RemoteVisitorService.class);
		RemoteSmtImageService imageRemote = Mockito.mock(RemoteSmtImageService.class);
		Mockito.when(visitorRemote.consumeVisitorActionCapability(Mockito.any(VisitorActionCapabilityConsumeReqDTO.class),
				Mockito.anyString(), Mockito.anyString())).thenReturn(Result.success(Boolean.TRUE));
		Mockito.when(imageRemote.saveImage(Mockito.any(SaveImageReqDto.class), Mockito.anyString()))
				.thenReturn(Result.success("photo-1"));
		VisitorServiceImpl service = service(visitorRemote, imageRemote);
		CheckFaceAo request = new CheckFaceAo();
		request.setVisitorPhoto("base64-image");

		assertEquals("photo-1", service.checkFace(request, "capability", "draft-1").getPhotoId());
		ArgumentCaptor<VisitorActionCapabilityConsumeReqDTO> consume = ArgumentCaptor.forClass(VisitorActionCapabilityConsumeReqDTO.class);
		Mockito.verify(visitorRemote).consumeVisitorActionCapability(consume.capture(), Mockito.eq(SecurityConstants.FROM_IN),
				Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		assertEquals(VisitorActionCapabilityAction.FACE_UPLOAD, consume.getValue().getAction());
		assertEquals("draft-1", consume.getValue().getDraftId());
		assertEquals(64, consume.getValue().getPayloadHash().length());
		Mockito.verify(imageRemote).saveImage(Mockito.any(SaveImageReqDto.class), Mockito.eq(SecurityConstants.FROM_IN));
	}

	@Test
	public void authenticatedEmployeeCannotUseLegacyBlacklistRouteWithoutCapability() {
		RemoteVisitorService visitorRemote = Mockito.mock(RemoteVisitorService.class);
		RemoteSmtImageService imageRemote = Mockito.mock(RemoteSmtImageService.class);
		SmartUser employee = new SmartUser(1, 1, "employee-1", Collections.singletonList(1), "N/A", true, true,
				true, true, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(employee, "N/A", employee.getAuthorities()));

		try {
			service(visitorRemote, imageRemote).checkBlackVisitor(new com.tce.smart.app.ao.wechat.AddVisitorAo(), null, null);
			fail("员工身份不得绕过访客黑名单 capability");
		} catch (org.springframework.security.access.AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(visitorRemote, imageRemote);
		}
	}

	private VisitorServiceImpl service(RemoteVisitorService visitorRemote, RemoteSmtImageService imageRemote) {
		return new VisitorServiceImpl(visitorRemote, null, null, imageRemote, null, null, null, null, null, null);
	}
}
