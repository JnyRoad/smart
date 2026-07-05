package com.tce.smart.platform.service.oacallback;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.OaCallbackLog;
import com.tce.smart.platform.support.RedisMutexLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * OA 回调分发器：request_id 级互斥锁内完成"查跳过集合 → 执行全部 handler → 落库/回写/关闭旧 partial"（spec §3.2.2）。
 * 单个 handler 失败不阻断其他 handler；存在失败时由 controller 返回 HTTP 500 交 OA 重试。
 */
@Slf4j
@Component
public class OaCallbackDispatcher {

	public static final String LOCK_KEY_PREFIX = "oa:callback:lock:";
	/** 锁 TTL：必须大于分发全程耗时上界（HANDLER_COUNT × MAX_HANDLER_SECONDS），见硬校验单测 */
	public static final long LOCK_TTL_SECONDS = 600;
	public static final int LOCK_RETRY_TIMES = 3;
	public static final long LOCK_RETRY_SLEEP_MS = 2000;
	public static final int HANDLER_COUNT = 12;
	/** 单 handler 最坏耗时上界（秒）：各外部调用均有显式超时的推导值 */
	public static final int MAX_HANDLER_SECONDS = 30;
	/** 大岭山转发超时（毫秒），修 D6 */
	public static final int FORWARD_TIMEOUT_MS = 5000;
	/** 由于OA系统回调地址只能配置一个，所有回调消息同步转发大岭山一份（原监听器 92 行迁移） */
	private static final String FORWARD_URL = "http://smartapp.szyuto.com:8080/platform/oa/workflow/over";

	// 设计不变量：各 handler 相互独立、按 processId 自行命中，执行顺序无关——新增 handler 不得引入顺序依赖
	private final List<OaWorkflowCallbackHandler> handlers;
	private final RedisMutexLock mutexLock;
	private final OaCallbackLogService logService;

	public OaCallbackDispatcher(List<OaWorkflowCallbackHandler> handlers,
			RedisMutexLock mutexLock, OaCallbackLogService logService) {
		this.handlers = handlers;
		this.mutexLock = mutexLock;
		this.logService = logService;
	}

	/** 处理一次 OA 回调（自然回调入口） */
	public DispatchResult dispatch(WorkFlowAO ao) {
		String requestId = ao.getRequestid();
		String payload = JSONUtil.toJsonStr(ao);
		log.info("收到OA审批消息：{}", payload);
		// 转发大岭山：加超时、失败仅告警，不影响本地处理与响应码（修 D6，锁外执行不占锁时长）
		forwardToDls(payload);
		// 入口先落库（独立事务，失败不阻断，spec §3.3）
		Long logId = logService.saveReceived(requestId, payload);
		// request_id 级互斥：串行化同单的自然回调 / OA 重推 / 重放（spec §3.2.2 四审 High-a/b）
		String lockKey = LOCK_KEY_PREFIX + requestId;
		String token = acquireWithRetry(lockKey);
		if (token == null) {
			log.error("OA回调获取request_id锁失败，返回500交OA重试：requestId={}", requestId);
			writeLockFailure(logId);
			return DispatchResult.builder().allSuccess(false).logId(logId)
					.failedHandlers(new ArrayList<>()).build();
		}
		long start = System.currentTimeMillis();
		try {
			OaCallbackLog oldPartial = logService.findLatestUnresolved(requestId);
			Set<String> skip = parseHandlerNames(oldPartial == null ? null : oldPartial.getSucceededHandlers());
			Set<String> succeeded = new LinkedHashSet<>(skip);
			List<String> failed = new ArrayList<>();
			String lastError = null;
			for (OaWorkflowCallbackHandler handler : handlers) {
				if (skip.contains(handler.name())) {
					continue;
				}
				try {
					handler.handle(requestId, ao);
					succeeded.add(handler.name());
				} catch (Exception e) {
					// 隔离：单业务失败不影响其他业务（spec §3.2.2）
					log.error("OA回调处理失败：requestId={}, handler={}", requestId, handler.name(), e);
					failed.add(handler.name());
					lastError = handler.name() + ": " + StrUtil.maxLength(String.valueOf(e), 500);
				}
			}
			// 关闭命中的旧 partial（无条件，spec §3.2.2 三审缺口 1）
			closeOldPartial(oldPartial);
			// 回写本次结果
			writeResult(logId, succeeded, failed, lastError, System.currentTimeMillis() - start);
			return DispatchResult.builder().allSuccess(failed.isEmpty()).logId(logId)
					.failedHandlers(failed).build();
		} finally {
			mutexLock.release(lockKey, token);
		}
	}

	private String acquireWithRetry(String lockKey) {
		for (int i = 0; i < LOCK_RETRY_TIMES; i++) {
			String token = mutexLock.acquire(lockKey, LOCK_TTL_SECONDS);
			if (token != null) {
				return token;
			}
			// 末次迭代不再 sleep：反正即将失败返回，多等一次只会白白拖慢向 OA 返回 500 的时间
			if (i < LOCK_RETRY_TIMES - 1) {
				try {
					Thread.sleep(LOCK_RETRY_SLEEP_MS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return null;
				}
			}
		}
		return null;
	}

	private void forwardToDls(String payload) {
		try {
			HttpUtil.createPost(FORWARD_URL).body(payload).timeout(FORWARD_TIMEOUT_MS).execute();
		} catch (Exception e) {
			// 转发失败不影响本地处理（spec §3.2.4），但不再静默吞掉
			log.warn("OA回调转发大岭山失败：{}", e.getMessage());
		}
	}

	private Set<String> parseHandlerNames(String joined) {
		Set<String> names = new LinkedHashSet<>();
		if (StrUtil.isNotBlank(joined)) {
			names.addAll(Arrays.asList(joined.split(",")));
		}
		return names;
	}

	private void closeOldPartial(OaCallbackLog oldPartial) {
		if (oldPartial == null) {
			return;
		}
		try {
			OaCallbackLog update = new OaCallbackLog();
			update.setId(oldPartial.getId());
			update.setResolved(OaCallbackLog.RESOLVED_YES);
			logService.updateById(update);
		} catch (Exception e) {
			// 关闭失败时若本次结果为 partial，写入将被函数唯一索引 ux_oa_cb_unresolved 拦截并降级为已解决快照（writeResult 兜底），不变量不破坏
			log.error("关闭历史partial回调日志失败：logId={}", oldPartial.getId(), e);
		}
	}

	private void writeLockFailure(Long logId) {
		if (logId == null) {
			return;
		}
		try {
			OaCallbackLog update = new OaCallbackLog();
			update.setId(logId);
			update.setLastError("acquire request_id lock timeout");
			logService.updateById(update);
		} catch (Exception e) {
			log.error("回写锁失败信息失败：logId={}", logId, e);
		}
	}

	private void writeResult(Long logId, Set<String> succeeded, List<String> failed, String lastError, long costMs) {
		if (logId == null) {
			return;
		}
		OaCallbackLog update = new OaCallbackLog();
		update.setId(logId);
		update.setSucceededHandlers(String.join(",", succeeded));
		update.setCostMs(costMs);
		if (failed.isEmpty()) {
			update.setStatus(OaCallbackLog.STATUS_SUCCESS);
			update.setResolved(OaCallbackLog.RESOLVED_YES);
		} else {
			update.setStatus(OaCallbackLog.STATUS_PARTIAL_FAIL);
			update.setResolved(OaCallbackLog.RESOLVED_NO);
			update.setFailedHandlers(String.join(",", failed));
			update.setLastError(lastError);
		}
		try {
			logService.updateById(update);
		} catch (Exception e) {
			// 函数唯一索引冲突（TTL 过期极端窗口）：宁可失败暴露，落为 resolved=1 失败快照（spec §3.2.2）
			log.error("回写回调结果冲突（疑似唯一索引拦截第二条未解决partial），落为已解决失败快照：logId={}", logId, e);
			update.setResolved(OaCallbackLog.RESOLVED_YES);
			try {
				logService.updateById(update);
			} catch (Exception e2) {
				log.error("回写失败快照仍失败：logId={}", logId, e2);
			}
		}
	}
}
