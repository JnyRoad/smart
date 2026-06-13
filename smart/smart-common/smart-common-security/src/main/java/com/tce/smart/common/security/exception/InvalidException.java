package com.tce.smart.common.security.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tce.smart.common.security.component.SmartAuth2ExceptionSerializer;

@JsonSerialize(using = SmartAuth2ExceptionSerializer.class)
public class InvalidException extends SmartAuth2Exception {

	public InvalidException(String msg, Throwable t) {
		super(msg);
	}

	@Override
	public String getOAuth2ErrorCode() {
		return "invalid_exception";
	}

	@Override
	public int getHttpErrorCode() {
		return 426;
	}

}
