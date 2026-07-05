package com.tce.smart.platform.service.oacallback.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.ao.WorkFlowRecordAO;
import com.tce.smart.platform.core.entity.SmtArticlesRelease;
import com.tce.smart.platform.service.SmtArticlesReleaseService;
import com.tce.smart.platform.service.SmtCallowanceCancelRecordService;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import com.tce.smart.platform.service.oacallback.ProcessRecordItem;
import com.tce.smart.platform.service.oacallback.ProcessRecordWriter;
import com.tce.smart.tool.enums.ArticlesReleaseStatusEnum;
import com.tce.smart.tool.enums.SmsTemplateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 办公区物品放行 OA 回调处理（原 LeaveApplicationListener 368-394 行等价搬迁）。
 * 特殊分支：不使用 OaFlowRecordSupport，记录循环无 flag 联动（全量写），
 * 回退判断直接取 workFlowAO.getStatus() 是否为 "1"，而非逐节点判断 RETURN。
 */
@Slf4j
@Component
public class ArticlesReleaseCallbackHandler implements OaWorkflowCallbackHandler {

	@Autowired
	private SmtArticlesReleaseService smtArticlesReleaseService;
	// 注意：物品放行审批通知借用了"外宿补贴撤销"的通知服务（callowanceCancelRecordService.approvalNotice），
	// 这是源代码既有写法（跨业务复用通知发送方法），此处照搬不做调整。
	@Autowired
	private SmtCallowanceCancelRecordService callowanceCancelRecordService;
	@Autowired
	private ProcessRecordWriter processRecordWriter;

	@Override
	public String name() {
		return "articlesRelease";
	}

	@Override
	public void handle(String processId, WorkFlowAO ao) {
		SmtArticlesRelease articlesRelease = smtArticlesReleaseService.getOne(new LambdaQueryWrapper<SmtArticlesRelease>()
				.eq(SmtArticlesRelease::getProcessId, processId));
		if (ObjectUtil.isNotNull(articlesRelease)) {
			List<WorkFlowRecordAO> flowRecords = ao.getFlowRecord();
			if (CollectionUtils.isNotEmpty(flowRecords)) {
				for (WorkFlowRecordAO flowRecord : flowRecords) {
					processRecordWriter.write(processId, ProcessRecordItem.fromCallback(flowRecord));
				}
			}
			// 是否回退
			boolean noReturn = "1".equals(ao.getStatus());
			log.info("物品放行流程编号【{}】审批完成", processId);
			// 归档
			if (noReturn) {
				//审批通过
				articlesRelease.setStatus(ArticlesReleaseStatusEnum.APPROVED.getCode());
				smtArticlesReleaseService.updateById(articlesRelease);
				// 发送APP消息通知
				callowanceCancelRecordService.approvalNotice(articlesRelease.getBadge(), SmsTemplateEnum.SMS_RELEASE_10605.getCode(), articlesRelease.getId().toString(), noReturn);
			} else {
				//审批回退
				articlesRelease.setStatus(ArticlesReleaseStatusEnum.APPROVAL_FAILED.getCode());
				smtArticlesReleaseService.updateById(articlesRelease);
				// 发送APP消息通知
				callowanceCancelRecordService.approvalNotice(articlesRelease.getBadge(), SmsTemplateEnum.SMS_RELEASE_10606.getCode(), articlesRelease.getId().toString(), noReturn);
			}
		}
	}
}
