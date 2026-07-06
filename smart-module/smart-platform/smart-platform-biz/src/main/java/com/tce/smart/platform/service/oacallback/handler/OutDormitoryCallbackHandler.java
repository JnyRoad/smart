package com.tce.smart.platform.service.oacallback.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.SmtOutDormitoryStaff;
import com.tce.smart.platform.service.SmtOutDormitoryStaffService;
import com.tce.smart.platform.service.oacallback.OaFlowRecordSupport;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import com.tce.smart.tool.enums.SmsTemplateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 外宿申请 OA 回调处理（原 LeaveApplicationListener 205-232 行等价搬迁） */
@Slf4j
@Component
public class OutDormitoryCallbackHandler implements OaWorkflowCallbackHandler {

	@Autowired
	private SmtOutDormitoryStaffService outDormitoryStaffService;
	@Autowired
	private OaFlowRecordSupport flowRecordSupport;

	@Override
	public String name() {
		return "outDormitory";
	}

	@Override
	public void handle(String processId, WorkFlowAO ao) {
		SmtOutDormitoryStaff outDormitoryStaff = outDormitoryStaffService.getOne(Wrappers.<SmtOutDormitoryStaff>query()
				.lambda().eq(SmtOutDormitoryStaff::getProcessId, processId));
		if (ObjectUtil.isNotNull(outDormitoryStaff)) {
			boolean flag = flowRecordSupport.processAndDetectReturn(processId, ao.getFlowRecord());
			log.info("流程编号【{}】审批完成", processId);
			if (flag) {
				//审批完成
				if (outDormitoryStaff.getAllowanceType().equals("外宿补贴"))
					outDormitoryStaffService.approvalNotice(outDormitoryStaff.getStaffBadge(), SmsTemplateEnum.APP_PUSH_6301.getCode(), outDormitoryStaff.getId(), flag);
				else
					outDormitoryStaffService.approvalNotice(outDormitoryStaff.getStaffBadge(), SmsTemplateEnum.APP_PUSH_6305.getCode(), outDormitoryStaff.getId(), flag);
			} else {
				//审批回退
				if (outDormitoryStaff.getAllowanceType().equals("外宿补贴"))
					outDormitoryStaffService.approvalNotice(outDormitoryStaff.getStaffBadge(), SmsTemplateEnum.APP_PUSH_6302.getCode(), outDormitoryStaff.getId(), flag);
				else
					outDormitoryStaffService.approvalNotice(outDormitoryStaff.getStaffBadge(), SmsTemplateEnum.APP_PUSH_6306.getCode(), outDormitoryStaff.getId(), flag);
			}
		}
	}
}
