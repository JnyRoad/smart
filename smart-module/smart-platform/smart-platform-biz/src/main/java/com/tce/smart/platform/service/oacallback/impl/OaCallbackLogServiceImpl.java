package com.tce.smart.platform.service.oacallback.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.OaCallbackLog;
import com.tce.smart.platform.core.mapper.OaCallbackLogMapper;
import com.tce.smart.platform.service.oacallback.OaCallbackLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** OA 回调日志服务实现 */
@Slf4j
@Service
public class OaCallbackLogServiceImpl extends ServiceImpl<OaCallbackLogMapper, OaCallbackLog> implements OaCallbackLogService {

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Long saveReceived(String requestId, String payload) {
		try {
			OaCallbackLog entity = new OaCallbackLog();
			entity.setRequestId(requestId);
			entity.setPayload(payload);
			entity.setReceiveTime(LocalDateTime.now());
			entity.setStatus(OaCallbackLog.STATUS_RECEIVED);
			entity.setResolved(OaCallbackLog.RESOLVED_NO);
			entity.setRetryCount(0);
			this.save(entity);
			return entity.getId();
		} catch (Exception e) {
			// 审计落库失败不能影响业务处理（spec §3.3）
			log.error("OA回调日志落库失败：requestId={}", requestId, e);
			return null;
		}
	}

	@Override
	public OaCallbackLog findLatestUnresolved(String requestId) {
		List<OaCallbackLog> list = this.list(Wrappers.<OaCallbackLog>query().lambda()
				.eq(OaCallbackLog::getRequestId, requestId)
				.eq(OaCallbackLog::getStatus, OaCallbackLog.STATUS_PARTIAL_FAIL)
				.eq(OaCallbackLog::getResolved, OaCallbackLog.RESOLVED_NO)
				.orderByDesc(OaCallbackLog::getReceiveTime)
				.orderByDesc(OaCallbackLog::getId));
		return list.isEmpty() ? null : list.get(0);
	}

	/** WARN 日志采样的 request_id 上限，防单条日志过长 */
	private static final int WARN_SAMPLE_SIZE = 10;

	@Override
	public int cleanExpiredLogs() {
		LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
		// 先统计将被删除的未解决 partial：90 天无人重放视为放弃，删除即丧失重放能力，必须 WARN 留痕
		List<OaCallbackLog> expiredUnresolved = this.list(Wrappers.<OaCallbackLog>query().lambda()
				.eq(OaCallbackLog::getStatus, OaCallbackLog.STATUS_PARTIAL_FAIL)
				.eq(OaCallbackLog::getResolved, OaCallbackLog.RESOLVED_NO)
				.lt(OaCallbackLog::getReceiveTime, cutoff));
		if (!expiredUnresolved.isEmpty()) {
			List<String> sampleRequestIds = expiredUnresolved.stream()
					.map(OaCallbackLog::getRequestId)
					.limit(WARN_SAMPLE_SIZE)
					.collect(Collectors.toList());
			log.warn("OA回调日志清理将删除未解决partial：count={}, requestIds(最多{}个)={}",
					expiredUnresolved.size(), WARN_SAMPLE_SIZE, sampleRequestIds);
		}
		// 90 天整行删除（payload 含 PII，到期物理删除，spec 2026-07-05 §2）
		// 直接取 delete 的受影响行数作为返回值（count 旁证在并发下可能与真实删除数不符）
		int deleted = this.getBaseMapper().delete(Wrappers.<OaCallbackLog>query().lambda()
				.lt(OaCallbackLog::getReceiveTime, cutoff));
		log.info("OA回调日志过期清理完成：deleted={}, cutoff={}", deleted, cutoff);
		return deleted;
	}
}
