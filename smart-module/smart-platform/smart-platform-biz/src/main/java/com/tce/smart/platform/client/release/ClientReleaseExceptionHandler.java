package com.tce.smart.platform.client.release;

import com.tce.smart.platform.client.identity.ClientApiException;
import com.tce.smart.platform.core.client.release.ReleaseRuleViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Collections;
import java.util.Map;

/** 放行 HTTP 层把领域拒绝转换为固定提示，绝不输出单据、卡号、SQL 或人员资料。 */
@Slf4j
@RestControllerAdvice(assignableTypes = ClientReleaseController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ClientReleaseExceptionHandler {
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handle(Exception failure) {
		int status = status(failure);
		if (status >= 500) log.error("物品放行发生未映射服务异常，类型={}", failure.getClass().getName());
		String message = status == 400 ? "请求格式无效" : status == 401 ? "请重新认证" : status == 403 ? "当前操作未获授权" : status == 404 ? "未找到有效单据" : status == 409 ? "单据状态已变化，请刷新后重试" : "物品放行服务暂不可用";
		return ResponseEntity.status(status).body(Collections.singletonMap("message", message));
	}
	private int status(Exception failure) {
		if (failure instanceof ClientApiException) return ((ClientApiException) failure).getStatus();
		if (failure instanceof HttpMessageNotReadableException) return 400;
		if (failure instanceof ReleaseRuleViolation) {
			switch (((ReleaseRuleViolation) failure).getCode()) {
				case INVALID_INPUT: case INVALID_ROUTE: case INVALID_SEAL: case INVALID_REJECTION_REASON: case INVALID_ESCORT: case INVALID_CARD_EVIDENCE: return 400;
				case MISSING_PERMISSION: case NOT_ASSIGNED_APPROVER: case SELF_APPROVAL: case UNAUTHORIZED_POST: return 403;
				default: return 409;
			}
		}
		return 503;
	}
}
