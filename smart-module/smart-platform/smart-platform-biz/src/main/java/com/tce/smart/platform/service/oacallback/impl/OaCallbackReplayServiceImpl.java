package com.tce.smart.platform.service.oacallback.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.OaCallbackLog;
import com.tce.smart.platform.service.oacallback.OaCallbackDispatcher;
import com.tce.smart.platform.service.oacallback.OaCallbackLogService;
import com.tce.smart.platform.service.oacallback.OaCallbackReplayService;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import com.tce.smart.platform.support.RedisMutexLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 重放实现：与自然回调共用同一把 request_id 锁（spec §3.3 v5 升级），
 * 锁内校验 status=2 and resolved=0，只重跑失败 handler，CAS 回写原记录。
 */
@Slf4j
@Service
public class OaCallbackReplayServiceImpl implements OaCallbackReplayService {

	private final List<OaWorkflowCallbackHandler> handlers;
	private final RedisMutexLock mutexLock;
	private final OaCallbackLogService logService;

	public OaCallbackReplayServiceImpl(List<OaWorkflowCallbackHandler> handlers,
			RedisMutexLock mutexLock, OaCallbackLogService logService) {
		this.handlers = handlers;
		this.mutexLock = mutexLock;
		this.logService = logService;
	}

	@Override
	public Result replay(Long logId) {
		OaCallbackLog logEntity = logService.getById(logId);
		if (logEntity == null) {
			return Result.fail("记录不存在");
		}
		if (!Integer.valueOf(OaCallbackLog.STATUS_PARTIAL_FAIL).equals(logEntity.getStatus())
				|| !Integer.valueOf(OaCallbackLog.RESOLVED_NO).equals(logEntity.getResolved())) {
			return Result.fail("已解决或状态不符，无需重放");
		}
		String lockKey = OaCallbackDispatcher.LOCK_KEY_PREFIX + logEntity.getRequestId();
		// 重放拿不到锁不重试，直接拒绝（spec §3.3）
		String token = mutexLock.acquire(lockKey, OaCallbackDispatcher.LOCK_TTL_SECONDS);
		if (token == null) {
			return Result.fail("正在处理，请稍后重试");
		}
		try {
			// 锁内二次校验，防止拿锁前状态已被自然回调改变
			logEntity = logService.getById(logId);
			if (logEntity == null
					|| !Integer.valueOf(OaCallbackLog.STATUS_PARTIAL_FAIL).equals(logEntity.getStatus())
					|| !Integer.valueOf(OaCallbackLog.RESOLVED_NO).equals(logEntity.getResolved())) {
				return Result.fail("已解决或状态不符，无需重放");
			}
			WorkFlowAO ao = JSONUtil.toBean(logEntity.getPayload(), WorkFlowAO.class);
			Set<String> succeeded = new LinkedHashSet<>();
			if (StrUtil.isNotBlank(logEntity.getSucceededHandlers())) {
				succeeded.addAll(Arrays.asList(logEntity.getSucceededHandlers().split(",")));
			}
			List<String> failed = new ArrayList<>();
			String lastError = null;
			for (OaWorkflowCallbackHandler handler : handlers) {
				if (succeeded.contains(handler.name())) {
					continue;
				}
				try {
					handler.handle(logEntity.getRequestId(), ao);
					succeeded.add(handler.name());
				} catch (Exception e) {
					log.error("重放处理失败：logId={}, handler={}", logId, handler.name(), e);
					failed.add(handler.name());
					lastError = handler.name() + ": " + StrUtil.maxLength(String.valueOf(e), 500);
				}
			}
			// CAS 回写原记录（where status=2 and resolved=0），不产生新 log
			boolean allOk = failed.isEmpty();
			boolean updated = logService.update(null, Wrappers.<OaCallbackLog>update().lambda()
					.eq(OaCallbackLog::getId, logId)
					.eq(OaCallbackLog::getStatus, OaCallbackLog.STATUS_PARTIAL_FAIL)
					.eq(OaCallbackLog::getResolved, OaCallbackLog.RESOLVED_NO)
					.set(OaCallbackLog::getRetryCount, logEntity.getRetryCount() + 1)
					.set(OaCallbackLog::getSucceededHandlers, String.join(",", succeeded))
					.set(OaCallbackLog::getFailedHandlers, allOk ? null : String.join(",", failed))
					// 全部成功后 lastError 随之清空（有意为之：终态记录不保留过期错误信息）
					.set(OaCallbackLog::getLastError, lastError)
					.set(OaCallbackLog::getStatus, allOk ? OaCallbackLog.STATUS_SUCCESS : OaCallbackLog.STATUS_PARTIAL_FAIL)
					.set(OaCallbackLog::getResolved, allOk ? OaCallbackLog.RESOLVED_YES : OaCallbackLog.RESOLVED_NO));
			if (!updated) {
				return Result.fail("回写冲突，请检查记录状态");
			}
			return allOk ? new Result<>(Boolean.TRUE, "重放成功")
					: Result.fail("重放后仍有失败：" + String.join(",", failed));
		} finally {
			mutexLock.release(lockKey, token);
		}
	}
}
