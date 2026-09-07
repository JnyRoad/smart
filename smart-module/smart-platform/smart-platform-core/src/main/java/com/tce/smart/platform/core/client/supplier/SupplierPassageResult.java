package com.tce.smart.platform.core.client.supplier;

/**
 * 纯规则计算得到的下一状态和独立事件。
 *
 * 调用方仍需在持久化层以事务和条件更新原子写入，不能把本结果视为已落库。
 */
public final class SupplierPassageResult {

	private final SupplierPresenceSnapshot presence;
	private final SupplierPassageEvent event;

	SupplierPassageResult(SupplierPresenceSnapshot presence, SupplierPassageEvent event) {
		this.presence = presence;
		this.event = event;
	}

	public SupplierPresenceSnapshot getPresence() {
		return presence;
	}

	public SupplierPassageEvent getEvent() {
		return event;
	}
}
