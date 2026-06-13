package com.tce.smart.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Mono;

/**
 * 请求参数解析器
 */
@Slf4j
public class SmartServerHttpResponseDecorator extends ServerHttpResponseDecorator {
	private ServerHttpRequest request;

	private String forward;

	private Long start;

	public SmartServerHttpResponseDecorator(ServerHttpRequest request, ServerHttpResponse response, String forward, Long start) {
		super(response);
		this.request = request;
		this.forward = forward;
		this.start = start;
	}

	@Override
	public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
		ServerHttpResponse response = this.getDelegate();
		HttpStatus httpStatus = response.getStatusCode();
		StringBuilder message = new StringBuilder();
		message
				.append("转发请求: Path=")
				.append(request.getPath())
				.append(", Method=")
				.append(request.getMethod())
				.append(", Forward=")
				.append(forward)
				.append(", Status=")
				.append(httpStatus);
		message
				.append(", Time=")
				.append(System.currentTimeMillis() - start)
				.append("ms");
		if (httpStatus == null || httpStatus.value() != HttpStatus.OK.value()) {
			log.error(message.toString());
		} else {
			log.info(message.toString());
		}
		return super.writeWith(body);
	}
}
