package com.tce.smart.platform.service.admittance;

import com.tce.smart.platform.api.dto.req.admittance.SaveAdmittanceCarApplyReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.VisitorTruckSmsVerifyReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorTruckProofRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;

/**
 * 匿名货车预约的短信 proof 生命周期。
 */
public interface VisitorTruckProofService {

	VisitorTruckProofRespDTO verifySms(VisitorTruckSmsVerifyReqDTO request);

	void assertActiveProof(String proofToken);

	SmtAdmittanceApply apply(String proofToken, SaveAdmittanceCarApplyReqDTO request);
}
