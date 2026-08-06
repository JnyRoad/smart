package com.tce.smart.app.service.fore;

import com.tce.smart.app.ao.wechat.CheckFaceAo;
import com.tce.smart.app.ao.wechat.AddVisitorAo;
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
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

	@Test
	public void capabilityForAnotherBlacklistIdentityFailsBeforeRemoteBlacklistQuery() {
		RemoteVisitorService visitorRemote = Mockito.mock(RemoteVisitorService.class);
		RemoteSmtImageService imageRemote = Mockito.mock(RemoteSmtImageService.class);
		Mockito.when(visitorRemote.consumeVisitorActionCapability(Mockito.any(VisitorActionCapabilityConsumeReqDTO.class),
				Mockito.anyString(), Mockito.anyString())).thenReturn(Result.fail("capability payload mismatch"));
		AddVisitorAo request = new AddVisitorAo();
		request.setVisitorName(" 张 三 ");
		request.setCertNo("110101199001010011");
		request.setParkId(1);

		try {
			service(visitorRemote, imageRemote).checkBlackVisitor(request, "ticket-for-other-identity", "draft-1");
			fail("身份摘要不匹配的 capability 不得查询黑名单");
		} catch (org.springframework.security.access.AccessDeniedException expected) {
			ArgumentCaptor<VisitorActionCapabilityConsumeReqDTO> consume = ArgumentCaptor.forClass(VisitorActionCapabilityConsumeReqDTO.class);
			Mockito.verify(visitorRemote).consumeVisitorActionCapability(consume.capture(), Mockito.eq(SecurityConstants.FROM_IN),
					Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
			assertEquals(VisitorActionCapabilityAction.BLACKLIST_CHECK, consume.getValue().getAction());
			assertEquals("5b012e396a3e0bc4c43b600a31d30d68b79a1f34f19aadf667e141a3a7c2440c",
					consume.getValue().getPayloadHash());
			Mockito.verify(visitorRemote, Mockito.never()).checkVisitorBlacklist(Mockito.any(), Mockito.anyString(),
					Mockito.anyString(), Mockito.anyString());
		}
	}

	/**
	 * 访客黑名单的身份证和车牌检查都必须经专用内部路由，并显式触发独立服务令牌。
	 */
	@Test
	public void blacklistFeignContractsRequireServiceTokenAndExactPurpose() throws Exception {
		assertBlacklistContract("checkVisitorBlacklist", "/internal/visitor-blacklist/visitor");
		assertBlacklistContract("checkVehicleBlacklist", "/internal/visitor-blacklist/vehicle");
	}

	@Test
	public void blacklistCapabilityUsesDedicatedServiceTokenAndPurpose() {
		RemoteVisitorService visitorRemote = Mockito.mock(RemoteVisitorService.class);
		RemoteSmtImageService imageRemote = Mockito.mock(RemoteSmtImageService.class);
		Mockito.when(visitorRemote.consumeVisitorActionCapability(Mockito.any(VisitorActionCapabilityConsumeReqDTO.class),
				Mockito.anyString(), Mockito.anyString())).thenReturn(Result.success(Boolean.TRUE));
		Mockito.when(visitorRemote.checkVisitorBlacklist(Mockito.any(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenReturn(Result.success(Boolean.TRUE));
		AddVisitorAo request = new AddVisitorAo();
		request.setVisitorName("张三");
		request.setCertNo(" 11010119900101001x ");
		request.setParkId(1);

		assertEquals(Boolean.TRUE, service(visitorRemote, imageRemote).checkBlackVisitor(request, "ticket", "draft-1").getData());
		ArgumentCaptor<com.tce.smart.platform.api.dto.SmtVisitorDTO> blacklistRequest = ArgumentCaptor.forClass(
				com.tce.smart.platform.api.dto.SmtVisitorDTO.class);
		Mockito.verify(visitorRemote).checkVisitorBlacklist(blacklistRequest.capture(), Mockito.eq(SecurityConstants.FROM_IN),
				Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED), Mockito.eq("visitor-blacklist"));
		assertEquals("11010119900101001X", blacklistRequest.getValue().getCertNo());
	}

	private void assertBlacklistContract(String methodName, String route) throws Exception {
		Method method = RemoteVisitorService.class.getMethod(methodName, com.tce.smart.platform.api.dto.SmtVisitorDTO.class,
				String.class, String.class, String.class);
		assertNotNull(method.getAnnotation(org.springframework.web.bind.annotation.PostMapping.class));
		assertEquals(route, method.getAnnotation(org.springframework.web.bind.annotation.PostMapping.class).value()[0]);
		assertEquals(SecurityConstants.FROM,
				method.getParameters()[1].getAnnotation(org.springframework.web.bind.annotation.RequestHeader.class).value());
		assertEquals(SecurityConstants.INTERNAL_SERVICE_AUTH,
				method.getParameters()[2].getAnnotation(org.springframework.web.bind.annotation.RequestHeader.class).value());
		assertEquals("X-Smart-Internal-Purpose",
				method.getParameters()[3].getAnnotation(org.springframework.web.bind.annotation.RequestHeader.class).value());
	}

	private VisitorServiceImpl service(RemoteVisitorService visitorRemote, RemoteSmtImageService imageRemote) {
		return new VisitorServiceImpl(visitorRemote, null, null, imageRemote, null, null, null, null, null, null);
	}
}
