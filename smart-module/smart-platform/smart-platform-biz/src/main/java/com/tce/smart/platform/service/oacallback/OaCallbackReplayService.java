package com.tce.smart.platform.service.oacallback;

import com.tce.smart.common.core.model.Result;

/** OA 回调重放服务（spec §3.3）：按 logId 只重跑失败 handler，回写原记录 */
public interface OaCallbackReplayService {
	Result replay(Long logId);
}
