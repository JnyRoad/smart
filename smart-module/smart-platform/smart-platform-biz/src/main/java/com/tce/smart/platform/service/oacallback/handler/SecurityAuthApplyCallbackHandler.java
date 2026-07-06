package com.tce.smart.platform.service.oacallback.handler;

import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import com.tce.smart.platform.service.oacallback.OaFlowRecordSupport;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthApplyService;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 保密区权限申请（门禁）OA 回调处理（原 LeaveApplicationListener 290-314 行等价搬迁）。
 * PR1 保持原逻辑（getByProcessId → 设 oaStatus → updateStatus），PR2 Task 23 改接 claim。
 */
@Slf4j
@Component
public class SecurityAuthApplyCallbackHandler implements OaWorkflowCallbackHandler {

	@Autowired
	private SmtSecurityAuthApplyService smtSecurityAuthApplyService;
	@Autowired
	private OaFlowRecordSupport flowRecordSupport;

	@Override
	public String name() {
		return "securityAuthApply";
	}

	@Override
	public void handle(String processId, WorkFlowAO ao) {
		SmtSecurityAuthApply authApply = smtSecurityAuthApplyService.getByProcessId(processId);
		if (Objects.nonNull(authApply)) {
			log.info("保密区权限申请收到OA推送【{}】", processId);
			boolean flag = flowRecordSupport.processAndDetectReturn(processId, ao.getFlowRecord());
			log.info("流程编号【{}】审批完成", processId);
			if (flag) {
				//审批通过
				authApply.setOaStatus(ApproveListStateEnum.AGREE.getCode());
			} else {
				//审批回退
				authApply.setOaStatus(ApproveListStateEnum.REFUSE.getCode());
			}
			smtSecurityAuthApplyService.updateStatus(authApply);
		}
	}
}
