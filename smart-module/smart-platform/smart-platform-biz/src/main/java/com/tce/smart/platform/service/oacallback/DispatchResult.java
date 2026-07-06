package com.tce.smart.platform.service.oacallback;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** 一次回调分发的聚合结果 */
@Data
@Builder
public class DispatchResult {
	/** 全部 handler 成功（含跳过） */
	private boolean allSuccess;
	/** 本次 smt_oa_callback_log 记录 id（落库失败为 null） */
	private Long logId;
	/** 失败 handler 名列表 */
	private List<String> failedHandlers;
}
