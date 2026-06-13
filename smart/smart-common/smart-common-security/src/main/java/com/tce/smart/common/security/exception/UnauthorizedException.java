package com.tce.smart.common.security.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tce.smart.common.security.component.SmartAuth2ExceptionSerializer;
import org.springframework.http.HttpStatus;

@JsonSerialize(using = SmartAuth2ExceptionSerializer.class)
public class UnauthorizedException extends SmartAuth2Exception {

	public UnauthorizedException(String msg, Throwable t) {
		super(msg);
	}

	@Override
	public String getOAuth2ErrorCode() {
		return "unauthorized";
	}

	@Override
	public int getHttpErrorCode() {
		return HttpStatus.UNAUTHORIZED.value();
	}

}
