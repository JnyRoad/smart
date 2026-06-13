package com.tce.smart.platform.service.approval.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.api.dto.req.approval.ApprovalProcessReqDTO;
import com.tce.smart.platform.api.dto.resp.approval.ApproveProcessListReqDTO;
import com.tce.smart.platform.core.entity.ApproveList;
import com.tce.smart.platform.core.entity.SmtApproval;
import com.tce.smart.platform.core.entity.SmtApprovalPerson;
import com.tce.smart.platform.core.service.SmtApprovalService;
import com.tce.smart.platform.service.approval.ApprovalNodeService;
import com.tce.smart.platform.service.approval.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:32
 */
@Service
public class ApprovalServiceImpl implements ApprovalService {

	@Autowired
	private SmtApprovalService smtApprovalService;
	@Autowired
	private ApprovalNodeService approvalNodeService;

	@Override
	public List<ApproveProcessListReqDTO>  approvalProcess(ApprovalProcessReqDTO reqDTO) {
		SmtApproval approval = smtApprovalService.getApproval(reqDTO.getParkId(), reqDTO.getEventId());
		if(Objects.isNull(approval)) {
			throw new SmartException("该园区未添加本流程审批设置");
		}
		reqDTO.setApprovalId(approval.getId());
		return approvalNodeService.getApprovalPerson(reqDTO);
	}

}
