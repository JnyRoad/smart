package com.tce.smart.platform.service.oacallback;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.OaCallbackLog;

/** OA 回调日志服务 */
public interface OaCallbackLogService extends IService<OaCallbackLog> {

	/**
	 * payload 留存天数：90 天整行删除。
	 * 与保密门禁对账回溯窗口（90 天）对齐；payload 含姓名/工号 PII，到期必须物理删除（spec 2026-07-05 §2）。
	 */
	int RETENTION_DAYS = 90;

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

	/**
	 * 清理 receive_time 早于 RETENTION_DAYS 天前的全部日志（整行删，含 payload）。
	 * 被删行中若含未解决 partial（status=2 and resolved=0）记 WARN——90 天无人重放视为放弃重放。
	 * @return 实际删除行数
	 */
	int cleanExpiredLogs();
}
