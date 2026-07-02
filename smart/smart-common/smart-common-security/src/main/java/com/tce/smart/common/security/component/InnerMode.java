package com.tce.smart.common.security.component;

/**
 * {@code @Inner} 内部接口校验模式。
 * <p>
 * 灰度顺序：AUDIT（观察期，只记录不拦截）→ ENFORCE（收口，正式拦截）；
 * OFF 等价于历史上切面被注释掉的状态，仅作紧急回滚兜底。
 */
public enum InnerMode {

	/** 关闭校验：完全放行，行为等价历史注释态（紧急回滚用） */
	OFF,

	/** 审计模式：缺少内部标识时只记 warn 日志，不拦截（灰度观察期，默认值） */
	AUDIT,

	/** 强制模式：缺少内部标识时拒绝访问（灰度收口后的目标态） */
	ENFORCE
}
