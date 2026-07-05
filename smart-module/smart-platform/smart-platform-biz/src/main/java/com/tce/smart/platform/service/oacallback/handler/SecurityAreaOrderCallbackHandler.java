package com.tce.smart.platform.service.oacallback.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.securityarea.SmtSecurityAreaOrder;
import com.tce.smart.platform.service.SmtSecurityAreaOrderService;
import com.tce.smart.platform.service.oacallback.OaFlowRecordSupport;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import com.tce.smart.tool.enums.SecurityAreaVisitStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 保密区预约 OA 回调处理（原 LeaveApplicationListener 266-288 行等价搬迁）。
 * 注意：原逻辑无论回退与否均置 PASSED，历史行为保留，待独立需求修复。
 */
@Slf4j
@Component
public class SecurityAreaOrderCallbackHandler implements OaWorkflowCallbackHandler {

	@Autowired
	private SmtSecurityAreaOrderService smtSecurityAreaOrderService;
	@Autowired
	private OaFlowRecordSupport flowRecordSupport;

	@Override
	public String name() {
		return "securityAreaOrder";
	}

	@Override
	public void handle(String processId, WorkFlowAO ao) {
		SmtSecurityAreaOrder smtSecurityAreaOrder = smtSecurityAreaOrderService.getOne(Wrappers.<SmtSecurityAreaOrder>query()
				.lambda().eq(SmtSecurityAreaOrder::getProcessId, processId));
		if (ObjectUtil.isNotNull(smtSecurityAreaOrder)) {
			boolean flag = flowRecordSupport.processAndDetectReturn(processId, ao.getFlowRecord());
			log.info("流程编号【{}】审批完成", processId);
			if (flag) {
				//审批通过
				SecurityAreaVisitStatusEnum statusEnum = SecurityAreaVisitStatusEnum.PASSED;
			} else {
				//审批回退
				SecurityAreaVisitStatusEnum statusEnum = SecurityAreaVisitStatusEnum.RETURNED;
			}
			smtSecurityAreaOrderService.update(SmtSecurityAreaOrder.builder().status(SecurityAreaVisitStatusEnum.PASSED.getCode()).build(),
					Wrappers.<SmtSecurityAreaOrder>update().lambda().eq(SmtSecurityAreaOrder::getProcessId, processId));
		}
	}
}
