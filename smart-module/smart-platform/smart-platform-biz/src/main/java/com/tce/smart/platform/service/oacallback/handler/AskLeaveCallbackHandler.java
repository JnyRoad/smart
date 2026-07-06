package com.tce.smart.platform.service.oacallback.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.SmtAskLeaveApplication;
import com.tce.smart.platform.service.SmtAskLeaveApplicationService;
import com.tce.smart.platform.service.oacallback.OaFlowRecordSupport;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import com.tce.smart.tool.enums.SmsTemplateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 请假申请 OA 回调处理（原 LeaveApplicationListener 123-142 行等价搬迁） */
@Slf4j
@Component
public class AskLeaveCallbackHandler implements OaWorkflowCallbackHandler {

	@Autowired
	private SmtAskLeaveApplicationService smtAskLeaveApplicationService;
	@Autowired
	private OaFlowRecordSupport flowRecordSupport;

	@Override
	public String name() {
		return "askLeave";
	}

	@Override
	public void handle(String processId, WorkFlowAO ao) {
		SmtAskLeaveApplication askLeaveApplication = smtAskLeaveApplicationService.getOne(Wrappers.<SmtAskLeaveApplication>query()
				.lambda().eq(SmtAskLeaveApplication::getProcessId, processId));
		if (ObjectUtil.isNull(askLeaveApplication)) {
			return;
		}
		boolean flag = flowRecordSupport.processAndDetectReturn(processId, ao.getFlowRecord());
		log.info("流程编号【{}】审批完成", processId);
		if (flag) {
			smtAskLeaveApplicationService.approvalNotice(askLeaveApplication.getStaffBadge(), SmsTemplateEnum.APP_PUSH_7301.getCode(), askLeaveApplication.getId());
		} else {
			smtAskLeaveApplicationService.approvalNotice(askLeaveApplication.getStaffBadge(), SmsTemplateEnum.APP_PUSH_7302.getCode(), askLeaveApplication.getId());
		}
	}
}
