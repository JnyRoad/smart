package com.tce.smart.platform.service.oacallback.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.SmtBreakoffApplication;
import com.tce.smart.platform.service.SmtAskLeaveApplicationService;
import com.tce.smart.platform.service.SmtBreakoffApplicationService;
import com.tce.smart.platform.service.oacallback.OaFlowRecordSupport;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import com.tce.smart.tool.enums.SmsTemplateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 调休申请 OA 回调处理（原 LeaveApplicationListener 183-203 行等价搬迁） */
@Slf4j
@Component
public class BreakoffCallbackHandler implements OaWorkflowCallbackHandler {

	@Autowired
	private SmtBreakoffApplicationService breakoffApplicationService;
	// 注意：原逻辑通知发送走的是请假 service 的 approvalNotice，等价搬迁保留（历史行为）
	@Autowired
	private SmtAskLeaveApplicationService smtAskLeaveApplicationService;
	@Autowired
	private OaFlowRecordSupport flowRecordSupport;

	@Override
	public String name() {
		return "breakoff";
	}

	@Override
	public void handle(String processId, WorkFlowAO ao) {
		SmtBreakoffApplication breakoffApplication = breakoffApplicationService.getOne(Wrappers.<SmtBreakoffApplication>query()
				.lambda().eq(SmtBreakoffApplication::getProcessId, processId));
		if (ObjectUtil.isNull(breakoffApplication)) {
			return;
		}
		boolean flag = flowRecordSupport.processAndDetectReturn(processId, ao.getFlowRecord());
		log.info("流程编号【{}】审批完成", processId);
		if (flag) {
			smtAskLeaveApplicationService.approvalNotice(breakoffApplication.getStaffBadge(), SmsTemplateEnum.APP_PUSH_8301.getCode(), breakoffApplication.getId());
		} else {
			smtAskLeaveApplicationService.approvalNotice(breakoffApplication.getStaffBadge(), SmsTemplateEnum.APP_PUSH_8302.getCode(), breakoffApplication.getId());
		}
	}
}
