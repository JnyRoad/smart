package com.tce.smart.platform.service.oacallback.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.service.SmtLeaveApplicationService;
import com.tce.smart.platform.service.ILeaveApplicationService;
import com.tce.smart.platform.service.oacallback.OaFlowRecordSupport;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 离职申请 OA 回调处理（原 LeaveApplicationListener 101-121 行等价搬迁） */
@Slf4j
@Component
public class LeaveApplicationCallbackHandler implements OaWorkflowCallbackHandler {

	@Autowired
	private SmtLeaveApplicationService smtLeaveApplicationService;
	@Autowired
	private ILeaveApplicationService leaveApplicationService;
	@Autowired
	private OaFlowRecordSupport flowRecordSupport;

	@Override
	public String name() {
		return "leaveApplication";
	}

	@Override
	public void handle(String processId, WorkFlowAO ao) {
		SmtLeaveApplication leaveApplication = smtLeaveApplicationService.getOne(Wrappers.<SmtLeaveApplication>query()
				.lambda().eq(SmtLeaveApplication::getProcessId, processId));
		if (ObjectUtil.isNull(leaveApplication)) {
			return;
		}
		boolean flag = flowRecordSupport.processAndDetectReturn(processId, ao.getFlowRecord());
		log.info("流程编号【{}】审批完成", processId);
		if (flag) {
			leaveApplicationService.endLeaveApplication(processId);
		} else {
			leaveApplicationService.failLeaveApplication(processId);
		}
	}
}
