package com.tce.smart.platform.service.oacallback;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.service.SmtProcessRecordService;
import com.tce.smart.tool.enums.NodeStatusEnum;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Component;

/**
 * 统一 smt_process_record 判重写入逻辑（原 LeaveApplicationListener.processRecord 的等价抽取；其余历史重复调用点的替换见后续任务）。
 * 逻辑与原 LeaveApplicationListener.processRecord 逐行等价，不做行为变更。
 */
@Component
public class ProcessRecordWriter {

	private final SmtProcessRecordService smtProcessRecordService;

	public ProcessRecordWriter(SmtProcessRecordService smtProcessRecordService) {
		this.smtProcessRecordService = smtProcessRecordService;
	}

	/** 判重写入一条过程记录；流程干预节点（logtype=i）跳过 */
	public void write(String processId, ProcessRecordItem item) {
		if (item.getLogtype().equals(NodeStatusEnum.INTERVENTION.getCode())) {
			return;
		}
		SmtProcessRecord processRecord = smtProcessRecordService.getOne(Wrappers.<SmtProcessRecord>query().lambda()
				.eq(SmtProcessRecord::getProcessId, processId)
				.eq(SmtProcessRecord::getStaffBadge, item.getWorkcode())
				.ne(SmtProcessRecord::getStatus, NodeStatusEnum.FINISHED.getCode())
				.ne(SmtProcessRecord::getStatus, NodeStatusEnum.NOT_FINISHED.getCode()));
		// 1、判重：不存在则新建
		if (ObjectUtil.isNull(processRecord)) {
			SmtProcessRecord record = new SmtProcessRecord();
			record.setCreatTime(DateUtil.date());
			record.setNodeName(item.getNodename());
			record.setProcessId(processId);
			String dateTime = item.getOperatedate() + " " + item.getOperatetime();
			if (StrUtil.isNotBlank(item.getOperatedate()) && StrUtil.isNotBlank(item.getOperatetime())) {
				record.setRecordDate(DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss"));
			}
			record.setRemark(htmlHandle(item.getRemark()));
			record.setStaffBadge(item.getWorkcode());
			record.setStaffName(item.getLastname());
			record.setStatus(item.getLogtype());
			smtProcessRecordService.save(record);
		} else {
			// 已存在且当前为"等待审批"状态 → 更新为最新节点状态
			if (processRecord.getStatus().equals(NodeStatusEnum.APPROVER.getCode())) {
				SmtProcessRecord record = new SmtProcessRecord();
				record.setId(processRecord.getId());
				String dateTime = item.getOperatedate() + " " + item.getOperatetime();
				if (StrUtil.isNotBlank(item.getOperatedate()) && StrUtil.isNotBlank(item.getOperatetime())) {
					record.setRecordDate(DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss"));
				}
				record.setStatus(item.getLogtype());
				record.setRemark(htmlHandle(item.getRemark()));
				smtProcessRecordService.updateById(record);
			}
		}
	}

	/** 去除 HTML 标签并反转义（等价原 htmlHandle） */
	private String htmlHandle(String html) {
		if (StrUtil.isBlank(html)) {
			return "";
		}
		String txtcontent = html.replaceAll("</?[^>]+>", "");
		txtcontent = txtcontent.replaceAll("<a>\\s*|\t|\r|\n</a>", "");
		return StringEscapeUtils.unescapeHtml(txtcontent).trim();
	}
}
