package com.tce.smart.platform.service.oacallback.handler;

import cn.hutool.core.util.ObjectUtil;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.oacallback.OaFlowRecordSupport;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import com.tce.smart.tool.enums.VisitorStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 入厂申请 OA 回调处理（原 LeaveApplicationListener 316-340 行等价搬迁） */
@Slf4j
@Component
public class AdmittanceApplyCallbackHandler implements OaWorkflowCallbackHandler {

	@Autowired
	private SmtAdmittanceApplyService smtAdmittanceApplyService;
	@Autowired
	private OaFlowRecordSupport flowRecordSupport;

	@Override
	public String name() {
		return "admittanceApply";
	}

	@Override
	public void handle(String processId, WorkFlowAO ao) {
		SmtAdmittanceApply admittanceApply = smtAdmittanceApplyService.getByProcessId(processId);
		// 原文照搬：枚举.equals(状态值)写法，仅当入厂申请处于"审批中"（Status_2）才处理本次回调
		if (ObjectUtil.isNotNull(admittanceApply) && VisitorStatusEnum.Status_2.equals(admittanceApply.getStatus())) {
			log.info("入厂申请：流程编号【{}】审批开始", processId);
			boolean flag = flowRecordSupport.processAndDetectReturn(processId, ao.getFlowRecord());
			if (flag) {
				//审批通过
				admittanceApply.setStatus(VisitorStatusEnum.Status_0.getCode());
			} else {
				//审批回退
				admittanceApply.setStatus(VisitorStatusEnum.Status_1.getCode());
			}
			smtAdmittanceApplyService.updateStatus(admittanceApply);
			log.info("入厂申请：流程编号【{}】审批完成", processId);
		}
	}
}
