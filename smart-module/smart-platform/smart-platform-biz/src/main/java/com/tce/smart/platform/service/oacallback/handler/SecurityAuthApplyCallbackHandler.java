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
 * 已接入 CAS claim 流程（PR2 行为变更，见 spec §3.2.1）：终态改由 claimOaFinalStatus
 * 原子抢占写入，只有抢到的一方才允许触发设备下发，避免回调与对账任务并发重复下发。
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
			// 通过走 AGREE，回退走 REFUSE；终态由 claim 原子抢占写入，不再直接 setOaStatus + updateStatus
			Integer finalStatus = flag ? ApproveListStateEnum.AGREE.getCode() : ApproveListStateEnum.REFUSE.getCode();
			if (smtSecurityAuthApplyService.claimOaFinalStatus(authApply.getId(), finalStatus)) {
				// 只有抢到终态的一方才继续，且仅审批通过才触发下发
				if (flag) {
					smtSecurityAuthApplyService.triggerDownDevice(authApply);
				}
			} else {
				// 终态已被对账任务或手动下发等并发路径抢先处理，属正常并发场景，非错误
				log.info("终态已被其他任务处理，跳过：processId={}", processId);
			}
		}
	}
}
