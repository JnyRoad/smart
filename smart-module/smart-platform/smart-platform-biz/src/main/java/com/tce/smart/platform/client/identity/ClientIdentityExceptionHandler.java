package com.tce.smart.platform.client.identity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Collections;
import java.util.Map;

/** 身份读取失败不泄露人员目录、账号状态或组织关系诊断。 */
@Slf4j
@RestControllerAdvice(assignableTypes = ClientIdentityController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ClientIdentityExceptionHandler {
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handle(Exception failure) {
		int status = failure instanceof ClientApiException ? ((ClientApiException) failure).getStatus() : 503;
		if (status >= 500) log.error("App 身份读取发生服务异常，类型={}", failure.getClass().getName());
		String message = status == 401 ? "请重新认证" : status == 403 ? "当前人员未获 App 使用资格" : "身份服务暂不可用";
		return ResponseEntity.status(status).body(Collections.singletonMap("message", message));
	}
}
