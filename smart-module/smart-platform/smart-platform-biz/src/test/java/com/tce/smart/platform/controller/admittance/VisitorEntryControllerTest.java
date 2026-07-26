package com.tce.smart.platform.controller.admittance;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction;
import com.tce.smart.platform.api.dto.req.admittance.SaveAdmittanceApplyReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.VisitorReceptionistSearchReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceApplyDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAreaOptionsRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorReceptionistRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceAreaOptionsService;
import com.tce.smart.platform.service.admittance.VisitorFaceCropCapabilityService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

/**
 * 匿名访客入口只能通过一次性草稿 capability 完成接待人查询和申请提交，身份标识始终留在服务端。
 */
public class VisitorEntryControllerTest {

	@Test
	public void receptionistSearchConsumesPayloadBoundCapabilityAndReturnsOnlyReceptionFields() {
		VisitorFaceCropCapabilityService capabilityService = Mockito.mock(VisitorFaceCropCapabilityService.class);
		SmtAdmittanceApplyService applyService = Mockito.mock(SmtAdmittanceApplyService.class);
		VisitorEntryController controller = new VisitorEntryController(capabilityService, applyService,
				Mockito.mock(SmtAdmittanceAreaOptionsService.class));
		VisitorReceptionistSearchReqDTO request = new VisitorReceptionistSearchReqDTO();
		request.setParkId(1);
		request.setReceptionistName("张 三");
		request.setReceptionistPhone("138 0000 0000");
		SmtAdmittanceApply matched = new SmtAdmittanceApply();
		matched.setReceptionistBadge("8031249");
		matched.setReceptionistName("张三");
		matched.setReceptionistPhone("13800000000");
		Mockito.when(applyService.searchReceptionist(Mockito.any(SmtAdmittanceApply.class))).thenReturn(matched);

		Result<VisitorReceptionistRespDTO> result = controller.searchReceptionist("search-ticket", "draft-1", request);

		Assert.assertEquals("8031249", result.getData().getReceptionistBadge());
		Assert.assertEquals("张三", result.getData().getReceptionistName());
		Assert.assertEquals("138****0000", result.getData().getReceptionistPhone());
		Mockito.verify(capabilityService).consumeActionCapability("search-ticket", "draft-1",
				VisitorActionCapabilityAction.RECEPTIONIST_SEARCH, controller.receptionistPayloadHash(request));
		Mockito.verify(capabilityService).rememberReceptionistSelection("draft-1", "8031249", "张三", "13800000000");
	}

	@Test
	public void submitConsumesDraftAndPayloadBoundCapabilityAndOverwritesBrowserUnionId() {
		VisitorFaceCropCapabilityService capabilityService = Mockito.mock(VisitorFaceCropCapabilityService.class);
		SmtAdmittanceApplyService applyService = Mockito.mock(SmtAdmittanceApplyService.class);
		VisitorEntryController controller = new VisitorEntryController(capabilityService, applyService,
				Mockito.mock(SmtAdmittanceAreaOptionsService.class));
		SaveAdmittanceApplyReqDTO request = new SaveAdmittanceApplyReqDTO();
		request.setUnionId("browser-controlled-union-id");
		request.setReceptionistBadge("browser-controlled-badge");
		Mockito.when(capabilityService.consumeReceptionistSelection("draft-token", "draft-1"))
				.thenReturn(new VisitorFaceCropCapabilityService.VisitorReceptionistSelection("8031249", "张三", "13800000000"));
		Mockito.when(capabilityService.resolveUnionId("draft-token", "draft-1")).thenReturn("server-union-id");
		Mockito.when(applyService.saveAdmittanceApply(request)).thenReturn(null);

		Result<AdmittanceApplyDetailRespDTO> result = controller.submitApply("submit-ticket", "draft-token", "draft-1", request);

		Assert.assertNotNull(result);
		Assert.assertEquals("server-union-id", request.getUnionId());
		Assert.assertEquals("8031249", request.getReceptionistBadge());
		Mockito.verify(capabilityService).consumeActionCapability(Mockito.eq("submit-ticket"), Mockito.eq("draft-1"),
				Mockito.eq(VisitorActionCapabilityAction.APPLY_SUBMIT), Mockito.matches("[0-9a-f]{64}"));
		Mockito.verify(capabilityService).resolveUnionId("draft-token", "draft-1");
		Mockito.verify(capabilityService).consumeReceptionistSelection("draft-token", "draft-1");
	}

	@Test
	public void applicationPayloadDigestMatchesTheBrowserCanonicalContract() {
		VisitorEntryController controller = new VisitorEntryController(Mockito.mock(VisitorFaceCropCapabilityService.class),
				Mockito.mock(SmtAdmittanceApplyService.class), Mockito.mock(SmtAdmittanceAreaOptionsService.class));
		SaveAdmittanceApplyReqDTO request = new SaveAdmittanceApplyReqDTO();
		request.setParkId(1);

		Assert.assertEquals("a2d63db45041e64c23f057e4ee196e8dc3e860efebc22a61bd1e32d205d373b0",
				controller.applyPayloadHash(request));
		request.setStartTime(LocalDateTime.of(2026, 7, 25, 10, 30));
		Assert.assertEquals("3fffbee7d8bf442ce98aa4789e0433c5efa80b30deff2066a679b9fe49d520d4",
				controller.applyPayloadHash(request));
	}

	@Test
	public void staticOptionsRequireTheOAuthDraftAccessGuard() {
		VisitorFaceCropCapabilityService capabilityService = Mockito.mock(VisitorFaceCropCapabilityService.class);
		SmtAdmittanceAreaOptionsService areaOptionsService = Mockito.mock(SmtAdmittanceAreaOptionsService.class);
		VisitorEntryController controller = new VisitorEntryController(capabilityService,
				Mockito.mock(SmtAdmittanceApplyService.class), areaOptionsService);
		AdmittanceAreaOptionsRespDTO options = new AdmittanceAreaOptionsRespDTO();
		options.setParkId(1);
		Mockito.when(areaOptionsService.getAreaOptions(1)).thenReturn(options);

		Result<AdmittanceAreaOptionsRespDTO> result = controller.getAreaOptions("draft-token", "draft-1", 1);

		Assert.assertSame(options, result.getData());
		Mockito.verify(capabilityService).assertStaticOptionAccess("draft-token", "draft-1");
	}
}
