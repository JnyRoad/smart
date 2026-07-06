package com.tce.smart.platform.service.oacallback;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.OaCallbackLog;

/** OA 回调日志服务 */
public interface OaCallbackLogService extends IService<OaCallbackLog> {

	/**
	 * 回调入口先落库（独立事务，失败仅记日志不阻断分发）。
	 * @return 新记录 id；落库失败返回 null
	 */
	Long saveReceived(String requestId, String payload);

	/**
	 * 查询同 request_id 最近一条未解决 partial（status=2 and resolved=0，
	 * order by receive_time desc, id desc，spec §3.2.2）。
	 */
	OaCallbackLog findLatestUnresolved(String requestId);
}
