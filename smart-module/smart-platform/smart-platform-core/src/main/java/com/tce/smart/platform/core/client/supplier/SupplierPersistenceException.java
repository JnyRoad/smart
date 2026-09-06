package com.tce.smart.platform.core.client.supplier;

/** 供应商通行仓储在持久化边界上的明确失败。 */
public final class SupplierPersistenceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public enum Code {
		INVALID_INPUT,
		PRESENCE_NOT_INITIALIZED,
		VERIFICATION_NOT_FOUND,
		VERIFICATION_CONSUMED,
		IDEMPOTENCY_CONFLICT,
		CONCURRENT_MODIFICATION,
		CORRUPT_DATA,
		STORAGE_FAILURE
	}

	private final Code code;

	SupplierPersistenceException(Code code, String message) {
		super(message);
		this.code = code;
	}

	SupplierPersistenceException(Code code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public Code getCode() {
		return code;
	}
}
