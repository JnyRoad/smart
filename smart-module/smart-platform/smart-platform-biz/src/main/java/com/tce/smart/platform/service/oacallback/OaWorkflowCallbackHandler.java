package com.tce.smart.platform.service.oacallback;

import com.tce.smart.platform.core.ao.WorkFlowAO;

/**
 * OA 工作流回调业务处理器：每个业务一个实现，内部自行按 processId 查表决定是否处理（spec §3.2.1）。
 * 实现约束：handle 内所有外部调用（HTTP/Feign）必须有显式超时，否则违反锁 TTL 上界推导前提（spec 终审 High）。
 */
public interface OaWorkflowCallbackHandler {

	/** handler 唯一名，写入 smt_oa_callback_log 的 succeeded/failed_handlers */
	String name();

	/** 处理一次 OA 回调；未命中本业务时应快速返回 */
	void handle(String processId, WorkFlowAO ao);
}
