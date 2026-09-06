package com.tce.smart.auth.client.session;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Collections;
import java.util.Map;

/** App 登录错误只返回固定消息，严禁回显工号、密码、上游异常或客户端配置。 */
@RestControllerAdvice(assignableTypes = ClientSessionController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ClientSessionExceptionHandler {
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handle(Exception failure) {
		int status = failure instanceof ClientSessionException ? ((ClientSessionException) failure).getStatus()
				: failure instanceof HttpMessageNotReadableException ? 400 : 503;
		String message = status == 400 ? "请求格式无效" : status == 401 ? "工号或密码错误" : "认证服务暂不可用";
		return ResponseEntity.status(status).body(Collections.singletonMap("message", message));
	}
}
