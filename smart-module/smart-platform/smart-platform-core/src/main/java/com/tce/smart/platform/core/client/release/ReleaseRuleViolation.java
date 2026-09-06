package com.tce.smart.platform.core.client.release;

/**
 * 保密物品放行领域规则拒绝结果。
 */
public final class ReleaseRuleViolation extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public enum Code {
		INVALID_INPUT,
		INVALID_ROUTE,
		INVALID_SEAL,
		MISSING_PERMISSION,
		VERSION_CONFLICT,
		INVALID_STATUS,
		NOT_ASSIGNED_APPROVER,
		SELF_APPROVAL,
		INVALID_REJECTION_REASON,
		UNAUTHORIZED_POST,
		INVALID_CARD_EVIDENCE,
		INVALID_ESCORT,
		ESCORT_METHOD_CHANGED,
		LOCK_ID_CHANGED
	}

	private final Code code;

	public ReleaseRuleViolation(Code code, String message) {
		super(message);
		this.code = code;
	}

	public Code getCode() {
		return code;
	}
}
