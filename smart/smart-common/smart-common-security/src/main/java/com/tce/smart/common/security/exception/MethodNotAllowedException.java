package com.tce.smart.common.security.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tce.smart.common.security.component.SmartAuth2ExceptionSerializer;
import org.springframework.http.HttpStatus;

@JsonSerialize(using = SmartAuth2ExceptionSerializer.class)
public class MethodNotAllowedException extends SmartAuth2Exception {

	public MethodNotAllowedException(String msg, Throwable t) {
		super(msg);
	}

	@Override
	public String getOAuth2ErrorCode() {
		return "method_not_allowed";
	}

	@Override
	public int getHttpErrorCode() {
		return HttpStatus.METHOD_NOT_ALLOWED.value();
	}

}
