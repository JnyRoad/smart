package com.tce.smart.common.core.exception;

import lombok.NoArgsConstructor;

/**
 * 403 授权拒绝
 */
@NoArgsConstructor
public class SmartDeniedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SmartDeniedException(String message) {
		super(message);
	}

	public SmartDeniedException(Throwable cause) {
		super(cause);
	}

	public SmartDeniedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SmartDeniedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
