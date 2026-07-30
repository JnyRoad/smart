package com.tce.smart.platform.security;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 已由遗留兼容过滤器验证的调用方园区范围，仅限当前请求使用。 */
public final class LegacyDoorLockCallerContext {

	public static final String REQUEST_ATTRIBUTE = LegacyDoorLockCallerContext.class.getName();

	private final String callerId;
	private final List<Integer> parkIds;

	public LegacyDoorLockCallerContext(String callerId, List<Integer> parkIds) {
		this.callerId = callerId;
		this.parkIds = Collections.unmodifiableList(new ArrayList<>(parkIds));
	}

	public String getCallerId() {
		return callerId;
	}

	public List<Integer> getParkIds() {
		return parkIds;
	}

	/** 兼容控制器只能接受过滤器写入的、范围非空的调用方上下文。 */
	public static LegacyDoorLockCallerContext require(HttpServletRequest request) {
		Object value = request.getAttribute(REQUEST_ATTRIBUTE);
		if (!(value instanceof LegacyDoorLockCallerContext)) {
			throw new IllegalStateException("遗留门锁调用方证明缺失");
		}
		LegacyDoorLockCallerContext context = (LegacyDoorLockCallerContext) value;
		if (context.parkIds.isEmpty()) {
			throw new IllegalStateException("遗留门锁调用方园区范围缺失");
		}
		return context;
	}
}
