package com.tce.smart.platform.service.admittance;

import com.tce.smart.platform.api.dto.req.admittance.VisitorSelfQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApplyRecordDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApprovalProgressRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorSelfQueryRespDTO;

public interface VisitorSelfQueryService {

	VisitorSelfQueryRespDTO listMyApply(VisitorSelfQueryReqDTO request, String queryToken);

	VisitorApplyRecordDetailRespDTO getApplyDetail(String applyId, String queryToken);

	VisitorApprovalProgressRespDTO getApprovalProgress(String applyId, String queryToken);

	/**
	 * 作废当前查询凭证所属访客的入厂申请。
	 *
	 * @param applyId    入厂申请单 ID
	 * @param queryToken 本人查询凭证
	 * @return 是否已作废并提交权限回收任务
	 */
	Boolean revokeApply(String applyId, String queryToken);
}
