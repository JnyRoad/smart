package com.tce.smart.platform.controller.admittance;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.admittance.SaveAdmittanceCarApplyReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.VisitorTruckSmsVerifyReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorTruckProofRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.service.admittance.VisitorTruckProofService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

public class VisitorTruckControllerTest {

	@Test
	public void controllerUsesDedicatedBasePathAndForwardsProofHeaderToProtectedOperations() throws Exception {
		VisitorTruckProofService proofService = Mockito.mock(VisitorTruckProofService.class);
		VisitorTruckController controller = new VisitorTruckController(proofService);
		RequestMapping mapping = VisitorTruckController.class.getAnnotation(RequestMapping.class);
		Assert.assertArrayEquals(new String[] {"/admittance/visitor-truck"}, mapping.value());

		VisitorTruckSmsVerifyReqDTO smsRequest = new VisitorTruckSmsVerifyReqDTO();
		VisitorTruckProofRespDTO proof = new VisitorTruckProofRespDTO();
		proof.setProof("proof-token");
		Mockito.when(proofService.verifySms(smsRequest)).thenReturn(proof);
		MockHttpServletResponse response = new MockHttpServletResponse();
		Result<VisitorTruckProofRespDTO> verifyResult = controller.verifySms(smsRequest, response);
		Assert.assertSame(proof, verifyResult.getData());
		Assert.assertEquals("no-store", response.getHeader("Cache-Control"));

		controller.causeOptions("proof-token");
		Mockito.verify(proofService).assertActiveProof("proof-token");

		SaveAdmittanceCarApplyReqDTO applyRequest = new SaveAdmittanceCarApplyReqDTO();
		SmtAdmittanceApply saved = new SmtAdmittanceApply();
		Mockito.when(proofService.apply("proof-token", applyRequest)).thenReturn(saved);
		Result<Boolean> applyResult = controller.apply("proof-token", applyRequest);
		Assert.assertEquals(Boolean.TRUE, applyResult.getData());
		Mockito.verify(proofService).apply("proof-token", applyRequest);
		assertProofHeader(VisitorTruckController.class.getMethod("causeOptions", String.class));
		assertProofHeader(VisitorTruckController.class.getMethod("apply", String.class, SaveAdmittanceCarApplyReqDTO.class));
	}

	private void assertProofHeader(Method method) {
		RequestHeader header = (RequestHeader) method.getParameterAnnotations()[0][0];
		Assert.assertEquals("X-Visitor-Truck-Sms-Proof", header.value());
		Assert.assertFalse(header.required());
	}
}
