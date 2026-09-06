package com.tce.smart.platform.core.client.supplier;

/**
 * 供应商通行领域规则拒绝结果。
 */
public final class SupplierRuleViolation extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public enum Code {
		INVALID_INPUT,
		MISSING_PERMISSION,
		UNAUTHORIZED_POST,
		INACTIVE_QUALIFICATION,
		ADMISSION_NOT_APPROVED,
		QUALIFICATION_NOT_YET_VALID,
		QUALIFICATION_EXPIRED,
		AREA_NOT_AUTHORIZED,
		PRESENCE_MISMATCH,
		VERIFICATION_MISMATCH,
		VERIFICATION_EXPIRED,
		VERSION_CONFLICT,
		DUPLICATE_DIRECTION
	}

	private final Code code;

	public SupplierRuleViolation(Code code, String message) {
		super(message);
		this.code = code;
	}

	public Code getCode() {
		return code;
	}
}
