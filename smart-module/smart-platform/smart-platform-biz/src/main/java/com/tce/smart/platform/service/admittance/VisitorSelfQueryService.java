package com.tce.smart.platform.service.admittance;

import com.tce.smart.platform.api.dto.req.admittance.VisitorSelfQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApplyRecordDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApprovalProgressRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorSelfQueryRespDTO;

public interface VisitorSelfQueryService {

	VisitorSelfQueryRespDTO listMyApply(VisitorSelfQueryReqDTO request, String queryToken);

	VisitorApplyRecordDetailRespDTO getApplyDetail(String applyId, String queryToken);

	VisitorApprovalProgressRespDTO getApprovalProgress(String applyId, String queryToken);
}
