package com.tce.smart.common.data.ratelimit;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RateLimitProperties} 默认值测试。
 *
 * <p>锁定“合并即零业务影响”的安全默认契约：未做任何配置时，限流整体不生效，
 * 单条规则默认观察模式 + fail-open。这些默认值是本基础设施安全性的基石，必须被测试守护，
 * 防止后续误改默认值导致悄悄改变线上业务行为。</p>
 */
public class RateLimitPropertiesTest {

	@Test
	public void defaults_areNoOpAndSafe() {
		RateLimitProperties properties = new RateLimitProperties();

		// 总开关默认关闭
		assertThat(properties.isEnabled()).isFalse();
		// 默认无任何规则
		assertThat(properties.getRules()).isEmpty();
	}

	@Test
	public void ruleDefaults_observeOnlyAndFailOpen() {
		RateLimitProperties.Rule rule = new RateLimitProperties.Rule();

		// 默认只观察不阻断
		assertThat(rule.isObserveOnly()).isTrue();
		// 默认 Redis 故障放行
		assertThat(rule.isFailOpen()).isTrue();
		// 默认阈值/窗口/锁定均为 0（计数维度 no-op）
		assertThat(rule.getThreshold()).isZero();
		assertThat(rule.getWindowSeconds()).isZero();
		assertThat(rule.getLockSeconds()).isZero();
		// 默认泛化提示，不暴露策略细节
		assertThat(rule.getErrorCode()).isEqualTo(429);
		assertThat(rule.getErrorMessage()).isEqualTo("请求过于频繁，请稍后再试");
	}
}
