package com.tce.smart.platform.service.oacallback.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.service.SmtVisitorService;
import com.tce.smart.platform.service.oacallback.OaFlowRecordSupport;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import com.tce.smart.tool.enums.VisitorStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** HF 访客预约 OA 回调处理（原 LeaveApplicationListener 342-366 行等价搬迁） */
@Slf4j
@Component
public class HfVisitorCallbackHandler implements OaWorkflowCallbackHandler {

	@Autowired
	private SmtVisitorService smtVisitorService;
	@Autowired
	private OaFlowRecordSupport flowRecordSupport;

	@Override
	public String name() {
		return "hfVisitor";
	}

	@Override
	public void handle(String processId, WorkFlowAO ao) {
		SmtVisitor visitor = smtVisitorService.getOne(Wrappers.<SmtVisitor>query()
				.lambda().eq(SmtVisitor::getProcessId, processId));
		// 原文照搬：枚举.equals(状态值)写法，仅当访客预约处于"审批中"（Status_2）才处理本次回调
		if (ObjectUtil.isNotNull(visitor) && VisitorStatusEnum.Status_2.equals(visitor.getStatus())) {
			log.info("合肥访客预约：流程编号【{}】审批开始", processId);
			boolean flag = flowRecordSupport.processAndDetectReturn(processId, ao.getFlowRecord());
			if (flag) {
				//审批通过
				visitor.setStatus(VisitorStatusEnum.Status_0.getCode());
			} else {
				//审批回退
				visitor.setStatus(VisitorStatusEnum.Status_1.getCode());
			}
			smtVisitorService.updateHfStatus(visitor);
			log.info("合肥访客预约：流程编号【{}】审批完成", processId);
		}
	}
}
