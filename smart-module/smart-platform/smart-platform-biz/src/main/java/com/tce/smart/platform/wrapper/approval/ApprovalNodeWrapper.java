package com.tce.smart.platform.wrapper.approval;


import cn.hutool.core.collection.CollUtil;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.approval.ApprovalConditionRespDTO;
import com.tce.smart.platform.api.dto.resp.approval.ApprovalNodeRespDTO;
import com.tce.smart.platform.api.dto.resp.approval.ApprovalPersonRespDTO;
import com.tce.smart.platform.core.entity.SmtApprovalCondition;
import com.tce.smart.platform.core.entity.SmtApprovalNode;
import com.tce.smart.platform.core.entity.SmtApprovalPerson;
import com.tce.smart.platform.core.service.SmtApprovalConditionService;
import com.tce.smart.platform.core.service.SmtApprovalPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;

/**
 * @Description: TODO
 * @ProjectName smart-platform
 * @ClassName: ApprovalNodeWrapper
 * @Author fushiping
 * @Date
 */
@Component
public class ApprovalNodeWrapper extends BaseWrapper<SmtApprovalNode, ApprovalNodeRespDTO> {

	@Autowired
	private SmtApprovalConditionService smtApprovalConditionService;
	@Autowired
	private SmtApprovalPersonService smtApprovalPersonService;

	@Override
	protected ApprovalNodeRespDTO warp(SmtApprovalNode model) throws IOException {
		ApprovalNodeRespDTO respDTO = BeanUtils.transform(ApprovalNodeRespDTO.class, model);
		Integer nodeId = model.getId();
		List<SmtApprovalCondition> smtApprovalConditions = smtApprovalConditionService.getList(nodeId);
		if(CollUtil.isNotEmpty(smtApprovalConditions)) {
			List<ApprovalConditionRespDTO> conditionList = BeanUtils.batchTransform(ApprovalConditionRespDTO.class, smtApprovalConditions);
			respDTO.setConditions(conditionList);
		}
		List<SmtApprovalPerson> personList = smtApprovalPersonService.getList(nodeId);
		if(CollUtil.isNotEmpty(personList)) {
			List<ApprovalPersonRespDTO> personRespDTOS = BeanUtils.batchTransform(ApprovalPersonRespDTO.class, personList);
			respDTO.setApprovalPersons(personRespDTOS);
		}
		return respDTO;
	}
}
