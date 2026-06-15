package com.tce.smart.common.data.ratelimit;

/**
 * 限流决策结果。
 *
 * <p>本对象只描述“限流怎么看这次请求”，<b>不替调用方做动作</b>：是否真正拒绝、返回什么响应，
 * 由调用方依据 {@link #isAllowed()} / {@link #isBlocked()} / {@link #isObserveOnly()} 自行决定。
 * 这样限流服务保持纯粹（只判定不副作用），调用方保留业务话语权。</p>
 */
public final class RateLimitDecision {

	/**
	 * 是否放行。observe-only、未超阈、未锁定、fail-open 兜底等情形均为 true。
	 */
	private final boolean allowed;

	/**
	 * 是否“达到了阻断条件”。注意：observe-only 模式下即便达到阻断条件，
	 * {@link #allowed} 仍为 true（只观察不阻断），但此字段为 true 以供日志/指标统计。
	 */
	private final boolean blocked;

	/**
	 * 是否处于观察模式（命中的规则 observeOnly=true）。
	 */
	private final boolean observeOnly;

	/**
	 * 当前窗口内的累计计数（命中规则时有意义）。无规则/异常兜底时为 0。
	 */
	private final long currentCount;

	/**
	 * 命中的规则阈值。无规则时为 0。
	 */
	private final long threshold;

	/**
	 * 命中的规则 ID。无规则时可能为 null。
	 */
	private final String ruleId;

	/**
	 * 阻断时建议返回的错误码（取自规则配置）。放行时为 0。
	 */
	private final int errorCode;

	/**
	 * 阻断时建议返回的泛化提示。放行时为 null。
	 */
	private final String errorMessage;

	private RateLimitDecision(boolean allowed, boolean blocked, boolean observeOnly,
							  long currentCount, long threshold, String ruleId,
							  int errorCode, String errorMessage) {
		this.allowed = allowed;
		this.blocked = blocked;
		this.observeOnly = observeOnly;
		this.currentCount = currentCount;
		this.threshold = threshold;
		this.ruleId = ruleId;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * 放行决策。用于：未命中规则、未超阈、fail-open 兜底等。
	 *
	 * @param ruleId       规则 ID（可为 null）
	 * @param currentCount 当前计数
	 * @param threshold    阈值
	 * @param observeOnly  是否观察模式
	 */
	public static RateLimitDecision allow(String ruleId, long currentCount, long threshold, boolean observeOnly) {
		return new RateLimitDecision(true, false, observeOnly, currentCount, threshold, ruleId, 0, null);
	}

	/**
	 * 阻断决策（强制模式下超阈/锁定时使用）。
	 *
	 * @param ruleId       规则 ID
	 * @param currentCount 当前计数
	 * @param threshold    阈值
	 * @param errorCode    建议错误码
	 * @param errorMessage 建议泛化提示
	 */
	public static RateLimitDecision block(String ruleId, long currentCount, long threshold,
										  int errorCode, String errorMessage) {
		return new RateLimitDecision(false, true, false, currentCount, threshold, ruleId, errorCode, errorMessage);
	}

	/**
	 * 观察模式命中阈值：标记 blocked=true 供统计，但仍放行（allowed=true）。
	 *
	 * @param ruleId       规则 ID
	 * @param currentCount 当前计数
	 * @param threshold    阈值
	 */
	public static RateLimitDecision observe(String ruleId, long currentCount, long threshold) {
		return new RateLimitDecision(true, true, true, currentCount, threshold, ruleId, 0, null);
	}

	public boolean isAllowed() {
		return allowed;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public boolean isObserveOnly() {
		return observeOnly;
	}

	public long getCurrentCount() {
		return currentCount;
	}

	public long getThreshold() {
		return threshold;
	}

	public String getRuleId() {
		return ruleId;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public String toString() {
		return "RateLimitDecision{ruleId=" + ruleId + ", allowed=" + allowed + ", blocked=" + blocked
			+ ", observeOnly=" + observeOnly + ", currentCount=" + currentCount + ", threshold=" + threshold + '}';
	}
}
