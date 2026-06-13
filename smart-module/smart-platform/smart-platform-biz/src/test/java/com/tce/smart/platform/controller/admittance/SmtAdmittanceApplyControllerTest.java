package com.tce.smart.platform.controller.admittance;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.admittance.VisitorSelfQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApplyRecordDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApprovalProgressRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorSelfQueryRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAreaOptionsRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorWechatIdentityRespDTO;
import com.tce.smart.platform.service.SmtVisitorService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceAreaOptionsService;
import com.tce.smart.platform.service.admittance.VisitorSelfQueryService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SmtAdmittanceApplyControllerTest {

	@Test
	public void getAreaOptionsDelegatesToAreaOptionsService() {
		SmtAdmittanceApplyService applyService = Mockito.mock(SmtAdmittanceApplyService.class);
		SmtVisitorService visitorService = Mockito.mock(SmtVisitorService.class);
		SmtAdmittanceAreaOptionsService areaOptionsService = Mockito.mock(SmtAdmittanceAreaOptionsService.class);
		VisitorSelfQueryService visitorSelfQueryService = Mockito.mock(VisitorSelfQueryService.class);
		AdmittanceAreaOptionsRespDTO options = new AdmittanceAreaOptionsRespDTO();
		options.setParkId(5000021);
		Mockito.when(areaOptionsService.getAreaOptions(5000021)).thenReturn(options);

		SmtAdmittanceApplyController controller = new SmtAdmittanceApplyController(applyService, visitorService, areaOptionsService,
				visitorSelfQueryService);
		Result<AdmittanceAreaOptionsRespDTO> result = controller.getAreaOptions(5000021);

		Assert.assertSame(options, result.getData());
		Mockito.verify(areaOptionsService).getAreaOptions(5000021);
		Mockito.verifyNoMoreInteractions(areaOptionsService);
	}

	@Test
	public void getOpenIdReturnsWechatIdentityObject() {
		SmtAdmittanceApplyService applyService = Mockito.mock(SmtAdmittanceApplyService.class);
		SmtVisitorService visitorService = Mockito.mock(SmtVisitorService.class);
		SmtAdmittanceAreaOptionsService areaOptionsService = Mockito.mock(SmtAdmittanceAreaOptionsService.class);
		VisitorSelfQueryService visitorSelfQueryService = Mockito.mock(VisitorSelfQueryService.class);
		VisitorWechatIdentityRespDTO expected = new VisitorWechatIdentityRespDTO();
		expected.setOpenId("openid-1");
		expected.setUnionId("unionid-1");
		Mockito.when(applyService.getOpenId("wx-code")).thenReturn(expected);

		SmtAdmittanceApplyController controller = new SmtAdmittanceApplyController(applyService, visitorService, areaOptionsService,
				visitorSelfQueryService);
		Result<VisitorWechatIdentityRespDTO> result = controller.getOpenId("wx-code");

		Assert.assertSame(expected, result.getData());
		Mockito.verify(applyService).getOpenId("wx-code");
		Mockito.verifyNoMoreInteractions(applyService);
	}

	@Test
	public void listMyApplyDelegatesToSelfQueryServiceWithQueryTokenHeader() {
		SmtAdmittanceApplyService applyService = Mockito.mock(SmtAdmittanceApplyService.class);
		SmtVisitorService visitorService = Mockito.mock(SmtVisitorService.class);
		SmtAdmittanceAreaOptionsService areaOptionsService = Mockito.mock(SmtAdmittanceAreaOptionsService.class);
		VisitorSelfQueryService visitorSelfQueryService = Mockito.mock(VisitorSelfQueryService.class);
		VisitorSelfQueryReqDTO request = new VisitorSelfQueryReqDTO();
		request.setMobile("13712341234");
		request.setSmsCode("123456");
		VisitorSelfQueryRespDTO expected = new VisitorSelfQueryRespDTO();
		expected.setQueryToken("tok-fixed");
		Mockito.when(visitorSelfQueryService.listMyApply(request, "tok-existing")).thenReturn(expected);

		SmtAdmittanceApplyController controller = new SmtAdmittanceApplyController(applyService, visitorService, areaOptionsService,
				visitorSelfQueryService);
		Result<VisitorSelfQueryRespDTO> result = controller.listMyApply(request, "tok-existing");

		Assert.assertSame(expected, result.getData());
		Mockito.verify(visitorSelfQueryService).listMyApply(request, "tok-existing");
		Mockito.verifyNoMoreInteractions(visitorSelfQueryService);
	}

	@Test
	public void applyDetailDelegatesToSelfQueryServiceWithQueryTokenHeader() {
		SmtAdmittanceApplyService applyService = Mockito.mock(SmtAdmittanceApplyService.class);
		SmtVisitorService visitorService = Mockito.mock(SmtVisitorService.class);
		SmtAdmittanceAreaOptionsService areaOptionsService = Mockito.mock(SmtAdmittanceAreaOptionsService.class);
		VisitorSelfQueryService visitorSelfQueryService = Mockito.mock(VisitorSelfQueryService.class);
		VisitorApplyRecordDetailRespDTO expected = new VisitorApplyRecordDetailRespDTO();
		expected.setApplyId("1001");
		Mockito.when(visitorSelfQueryService.getApplyDetail("1001", "tok-existing")).thenReturn(expected);

		SmtAdmittanceApplyController controller = new SmtAdmittanceApplyController(applyService, visitorService, areaOptionsService,
				visitorSelfQueryService);
		Result<VisitorApplyRecordDetailRespDTO> result = controller.applyDetail("1001", "tok-existing");

		Assert.assertSame(expected, result.getData());
		Mockito.verify(visitorSelfQueryService).getApplyDetail("1001", "tok-existing");
		Mockito.verifyNoMoreInteractions(visitorSelfQueryService);
	}

	@Test
	public void approvalProgressDelegatesToSelfQueryServiceWithQueryTokenHeader() {
		SmtAdmittanceApplyService applyService = Mockito.mock(SmtAdmittanceApplyService.class);
		SmtVisitorService visitorService = Mockito.mock(SmtVisitorService.class);
		SmtAdmittanceAreaOptionsService areaOptionsService = Mockito.mock(SmtAdmittanceAreaOptionsService.class);
		VisitorSelfQueryService visitorSelfQueryService = Mockito.mock(VisitorSelfQueryService.class);
		VisitorApprovalProgressRespDTO expected = new VisitorApprovalProgressRespDTO();
		Mockito.when(visitorSelfQueryService.getApprovalProgress("1001", "tok-existing")).thenReturn(expected);

		SmtAdmittanceApplyController controller = new SmtAdmittanceApplyController(applyService, visitorService, areaOptionsService,
				visitorSelfQueryService);
		Result<VisitorApprovalProgressRespDTO> result = controller.approvalProgress("1001", "tok-existing");

		Assert.assertSame(expected, result.getData());
		Mockito.verify(visitorSelfQueryService).getApprovalProgress("1001", "tok-existing");
		Mockito.verifyNoMoreInteractions(visitorSelfQueryService);
	}
}
