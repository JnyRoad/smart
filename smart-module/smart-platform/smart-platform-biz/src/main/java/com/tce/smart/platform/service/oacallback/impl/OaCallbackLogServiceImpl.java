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
}
