package com.tce.smart.platform.core.dto.authgovernance;

/** 同一幂等键携带了不同规范请求，HTTP 层必须映射为409。 */
public class AuthOperationGovernanceConflictException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AuthOperationGovernanceConflictException(String message) {
		super(message);
	}
}
