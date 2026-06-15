package com.tce.smart.common.data.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 限流配置。
 *
 * <p>前缀 {@code smart.security.rate-limit}，按规则 ID 维护一组限流规则。</p>
 *
 * <p>默认形态刻意保证“合并即零业务影响”：</p>
 * <ul>
 *   <li>{@link #enabled} 默认 {@code false}：整体不启用，任何调用都直接放行；</li>
 *   <li>{@link #rules} 默认空：即使 enabled=true，没有匹配规则的 ruleId 也直接放行（no-op）；</li>
 *   <li>单条规则 {@link Rule#observeOnly} 默认 {@code true}：只观察记数、不真正阻断；</li>
 *   <li>单条规则 {@link Rule#failOpen} 默认 {@code true}：Redis 故障时放行而非误伤业务。</li>
 * </ul>
 * 这样在没有人工显式配置阈值之前，本组件不会改变任何既有业务行为。
 */
@Data
@ConfigurationProperties(prefix = "smart.security.rate-limit")
public class RateLimitProperties {

	/**
	 * 限流总开关。默认关闭，保证未配置时对业务零影响。
	 */
	private boolean enabled = false;

	/**
	 * 限流规则表，key 为业务自定义的规则 ID（如 sms-send、login-fail）。
	 * 用 {@link LinkedHashMap} 保持配置书写顺序，便于排查。
	 */
	private Map<String, Rule> rules = new LinkedHashMap<>();

	/**
	 * 单条限流规则。
	 */
	@Data
	public static class Rule {

		/**
		 * 观察模式开关。默认 {@code true}：只累计计数、记日志/指标，但不阻断、不锁定，
		 * 决策结果始终为放行。用于上线初期灰度观察真实流量，确认阈值合理后再切为强制阻断。
		 */
		private boolean observeOnly = true;

		/**
		 * 滑动计数窗口内允许的最大请求次数。窗口内累计次数超过该阈值即判定为超限。
		 * {@code <= 0} 表示不限制次数（计数维度的 no-op）。
		 */
		private long threshold = 0L;

		/**
		 * 计数窗口长度（秒）。首次计数时为该 key 设置 TTL，窗口到期后计数自动归零，
		 * 实现“每 windowSeconds 秒最多 threshold 次”的固定窗口限流。
		 */
		private long windowSeconds = 0L;

		/**
		 * 失败锁定时长（秒）。当失败次数（见 {@link RateLimitService#recordFailure}）达到
		 * {@link #threshold} 时，写入一个 TTL 为该值的锁定标记，锁定期内 {@code isLocked} 返回 true。
		 * {@code <= 0} 表示不启用失败锁定。
		 */
		private long lockSeconds = 0L;

		/**
		 * Redis 故障时是否“放行（fail-open）”。默认 {@code true}：限流自身的基础设施故障
		 * 绝不应该阻断正常业务，宁可短暂失去保护也不误伤用户。设为 {@code false} 则改为
		 * “拒绝（fail-close）”，仅用于对安全性要求极高、宁可错杀的场景。
		 */
		private boolean failOpen = true;

		/**
		 * 阻断时回传给调用方的错误码，便于调用方构造统一响应。默认 429（Too Many Requests）。
		 */
		private int errorCode = 429;

		/**
		 * 阻断时回传给调用方的提示文案。刻意使用泛化措辞，不暴露具体阈值/枚举细节，
		 * 避免被用于探测限流策略。
		 */
		private String errorMessage = "请求过于频繁，请稍后再试";
	}
}
