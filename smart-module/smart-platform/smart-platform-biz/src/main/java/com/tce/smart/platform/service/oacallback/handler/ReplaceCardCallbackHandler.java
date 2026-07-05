package com.tce.smart.platform.service.oacallback.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.SmtReplaceApplication;
import com.tce.smart.platform.service.SmtAskLeaveApplicationService;
import com.tce.smart.platform.service.SmtReplaceApplicationService;
import com.tce.smart.platform.service.oacallback.OaFlowRecordSupport;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import com.tce.smart.tool.enums.SmsTemplateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 补卡申请 OA 回调处理（原 LeaveApplicationListener 163-182 行等价搬迁） */
@Slf4j
@Component
public class ReplaceCardCallbackHandler implements OaWorkflowCallbackHandler {

	@Autowired
	private SmtReplaceApplicationService smtReplaceApplicationService;
	// 注意：原逻辑通知发送走的是请假 service 的 approvalNotice，等价搬迁保留（历史行为）
	@Autowired
	private SmtAskLeaveApplicationService smtAskLeaveApplicationService;
	@Autowired
	private OaFlowRecordSupport flowRecordSupport;

	@Override
	public String name() {
		return "replaceCard";
	}

	@Override
	public void handle(String processId, WorkFlowAO ao) {
		SmtReplaceApplication replaceApplication = smtReplaceApplicationService.getOne(Wrappers.<SmtReplaceApplication>query()
				.lambda().eq(SmtReplaceApplication::getProcessId, processId));
		if (ObjectUtil.isNull(replaceApplication)) {
			return;
		}
		boolean flag = flowRecordSupport.processAndDetectReturn(processId, ao.getFlowRecord());
		log.info("流程编号【{}】审批完成", processId);
		if (flag) {
			smtAskLeaveApplicationService.approvalNotice(replaceApplication.getStaffBadge(), SmsTemplateEnum.APP_PUSH_10301.getCode(), replaceApplication.getId());
		} else {
			smtAskLeaveApplicationService.approvalNotice(replaceApplication.getStaffBadge(), SmsTemplateEnum.APP_PUSH_10303.getCode(), replaceApplication.getId());
		}
	}
}
