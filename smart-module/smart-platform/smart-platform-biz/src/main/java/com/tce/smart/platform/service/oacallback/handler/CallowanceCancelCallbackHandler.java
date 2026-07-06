package com.tce.smart.platform.service.oacallback.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.ao.WorkFlowRecordAO;
import com.tce.smart.platform.core.entity.SmtCallowanceCancelRecord;
import com.tce.smart.platform.service.SmtCallowanceCancelRecordService;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import com.tce.smart.platform.service.oacallback.ProcessRecordItem;
import com.tce.smart.platform.service.oacallback.ProcessRecordWriter;
import com.tce.smart.tool.enums.NodeStatusEnum;
import com.tce.smart.tool.enums.SmsTemplateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 外宿补贴撤销 OA 回调处理（原 LeaveApplicationListener 233-264 行等价搬迁）。
 * 特殊分支：不使用 OaFlowRecordSupport，回退判断取"最后一个节点"而非逐节点判断，逻辑逐行照搬。
 */
@Slf4j
@Component
public class CallowanceCancelCallbackHandler implements OaWorkflowCallbackHandler {

	@Autowired
	private SmtCallowanceCancelRecordService callowanceCancelRecordService;
	@Autowired
	private ProcessRecordWriter processRecordWriter;

	@Override
	public String name() {
		return "callowanceCancel";
	}

	@Override
	public void handle(String processId, WorkFlowAO ao) {
		SmtCallowanceCancelRecord callowanceCancelRecord = callowanceCancelRecordService.getOne(Wrappers.<SmtCallowanceCancelRecord>query()
				.lambda().eq(SmtCallowanceCancelRecord::getProcessId, processId));
		if (ObjectUtil.isNotNull(callowanceCancelRecord)) {
			boolean flag = true; //是否回退
			List<WorkFlowRecordAO> flowRecords = ao.getFlowRecord();

			log.info("callowanceCancelRecordService-flowRecords:" + flowRecords);
			if (CollectionUtils.isNotEmpty(flowRecords)) {
				//直接判断最后一个节点
				WorkFlowRecordAO lastAo = flowRecords.get(flowRecords.size() - 1);
				log.info("lastAo：", lastAo);
				if (lastAo.getLogtype().equals(NodeStatusEnum.RETURN.getCode())) {
					flag = false;
				}
				for (WorkFlowRecordAO flowRecord : flowRecords) {
					/*if(flag) {
					flag = !NodeStatusEnum.RETURN.getCode().equals(flowRecord.getLogtype());
				}*/
					processRecordWriter.write(processId, ProcessRecordItem.fromCallback(flowRecord));
				}
			}
			log.info("流程编号【{}】审批完成", processId);
			if (flag) {
				//审批完成
				callowanceCancelRecordService.approvalNotice(callowanceCancelRecord.getBadge(), SmsTemplateEnum.APP_PUSH_6303.getCode(), callowanceCancelRecord.getId().toString(), flag);
			} else {
				//审批回退
				callowanceCancelRecordService.approvalNotice(callowanceCancelRecord.getBadge(), SmsTemplateEnum.APP_PUSH_6304.getCode(), callowanceCancelRecord.getId().toString(), flag);
			}
		}
	}
}
