package com.tce.smart.platform.service.oacallback;

import com.tce.smart.platform.core.ao.WorkFlowRecordAO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import lombok.Builder;
import lombok.Data;

/**
 * 过程记录归一化 DTO：统一回调（WorkFlowRecordAO）与 OA 查询（WorkFlowLogDataDTO）两套入参（spec §3.2.4）
 */
@Data
@Builder
public class ProcessRecordItem {
	private String workcode;
	private String lastname;
	private String nodename;
	private String logtype;
	private String operatedate;
	private String operatetime;
	private String remark;

	/** 从 OA 回调记录转换 */
	public static ProcessRecordItem fromCallback(WorkFlowRecordAO ao) {
		return ProcessRecordItem.builder()
				.workcode(ao.getWorkcode()).lastname(ao.getLastname())
				.nodename(ao.getNodename()).logtype(ao.getLogtype())
				.operatedate(ao.getOperatedate()).operatetime(ao.getOperatetime())
				.remark(ao.getRemark()).build();
	}

	/** 从 OA 查询流转记录转换（注意该 DTO 字段为全大写命名） */
	public static ProcessRecordItem fromOaLog(WorkFlowLogDataDTO dto) {
		return ProcessRecordItem.builder()
				.workcode(dto.getWORKCODE()).lastname(dto.getLASTNAME())
				.nodename(dto.getNODENAME()).logtype(dto.getLOGTYPE())
				.operatedate(dto.getOPERATEDATE()).operatetime(dto.getOPERATETIME())
				.remark(dto.getREMARK()).build();
	}
}
