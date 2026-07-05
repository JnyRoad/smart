package com.tce.smart.platform.service.oacallback.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.SmtOvertimeApplication;
import com.tce.smart.platform.service.SmtAskLeaveApplicationService;
import com.tce.smart.platform.service.SmtOvertimeApplicationService;
import com.tce.smart.platform.service.oacallback.OaFlowRecordSupport;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import com.tce.smart.tool.enums.SmsTemplateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 加班申请 OA 回调处理（原 LeaveApplicationListener 143-162 行等价搬迁） */
@Slf4j
@Component
public class OvertimeCallbackHandler implements OaWorkflowCallbackHandler {

	@Autowired
	private SmtOvertimeApplicationService smtOvertimeApplicationService;
	// 注意：原逻辑通知发送走的是请假 service 的 approvalNotice，等价搬迁保留（历史行为）
	@Autowired
	private SmtAskLeaveApplicationService smtAskLeaveApplicationService;
	@Autowired
	private OaFlowRecordSupport flowRecordSupport;

	@Override
	public String name() {
		return "overtime";
	}

	@Override
	public void handle(String processId, WorkFlowAO ao) {
		SmtOvertimeApplication overtimeApplication = smtOvertimeApplicationService.getOne(Wrappers.<SmtOvertimeApplication>query()
				.lambda().eq(SmtOvertimeApplication::getProcessId, processId));
		if (ObjectUtil.isNull(overtimeApplication)) {
			return;
		}
		boolean flag = flowRecordSupport.processAndDetectReturn(processId, ao.getFlowRecord());
		log.info("流程编号【{}】审批完成", processId);
		if (flag) {
			smtAskLeaveApplicationService.approvalNotice(overtimeApplication.getStaffBadge(), SmsTemplateEnum.APP_PUSH_9301.getCode(), overtimeApplication.getId());
		} else {
			smtAskLeaveApplicationService.approvalNotice(overtimeApplication.getStaffBadge(), SmsTemplateEnum.APP_PUSH_9302.getCode(), overtimeApplication.getId());
		}
	}
}
