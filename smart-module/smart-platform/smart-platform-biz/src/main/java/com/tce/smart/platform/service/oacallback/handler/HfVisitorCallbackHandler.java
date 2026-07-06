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

/**
 * HF 访客预约 OA 回调处理（源自 LeaveApplicationListener 342-366 行搬迁）。
 * 与原文的差异：修复了"枚举实例.equals(Integer 状态值)"恒 false 的死分支，
 * 并把终态落库改走 claim 式幂等入口，与拉取对账（updateOaStatusTask）互斥。
 */
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
		// 待审核判断用 Integer code 比较；历史写法"枚举实例.equals(Integer)"恒为 false，导致本分支从未执行（状态实际靠拉取对账推进）
		if (ObjectUtil.isNull(visitor)
				|| !VisitorStatusEnum.Status_2.getCode().equals(visitor.getStatus())) {
			return;
		}
		log.info("合肥访客预约：流程编号【{}】审批开始", processId);
		//回退节点存在 → 审批拒绝；否则审批通过
		boolean flag = flowRecordSupport.processAndDetectReturn(processId, ao.getFlowRecord());
		Integer finalStatus = flag ? VisitorStatusEnum.Status_0.getCode() : VisitorStatusEnum.Status_1.getCode();
		// claim 式幂等：与拉取对账（updateOaStatusTask）共用 CAS 抢占入口，避免回调与定时对账并发重复下发/重复短信
		if (!smtVisitorService.claimAndApplyHfOaFinalStatus(visitor, finalStatus)) {
			log.info("合肥访客预约：流程编号【{}】终态已被对账任务处理，跳过本次回调", processId);
			return;
		}
		log.info("合肥访客预约：流程编号【{}】审批完成", processId);
	}
}
