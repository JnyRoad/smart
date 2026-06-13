package com.tce.smart.bridge.core.exception;

import com.tce.smart.bridge.core.enums.ExceptionEnum;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class WrapperException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public WrapperException(String message) {
		super(message);
	}

	public WrapperException(ExceptionEnum exception) {
		super(exception.getDesc());
	}

	public WrapperException(Throwable cause) {
		super(cause);
	}

	public WrapperException(String message, Throwable cause) {
		super(message, cause);
	}

	public WrapperException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
