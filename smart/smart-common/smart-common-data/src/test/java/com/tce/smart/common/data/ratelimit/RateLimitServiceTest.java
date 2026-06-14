package com.tce.smart.common.data.ratelimit;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RateLimitService} 单元测试。
 *
 * <p>通过 mock {@link StringRedisTemplate} 隔离真实 Redis，覆盖：未超阈放行、超阈阻断、
 * 窗口过期重置、失败计数锁定、clear 清除、observe-only 只标记不阻断、fail-open、无规则 no-op。</p>
 */
public class RateLimitServiceTest {

	private StringRedisTemplate redisTemplate;
	private ValueOperations<String, String> valueOps;
	private RateLimitProperties properties;
	private RateLimitService service;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		redisTemplate = mock(StringRedisTemplate.class);
		valueOps = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOps);
		properties = new RateLimitProperties();
		// 限流服务仅依赖 Redis 与配置，观测走日志，不依赖 micrometer/actuator。
		service = new RateLimitService(redisTemplate, properties);
	}

	/**
	 * 构造一条强制阻断（observeOnly=false）的规则并注册到配置。
	 */
	private void enforceRule(String ruleId, long threshold, long windowSeconds, long lockSeconds) {
		properties.setEnabled(true);
		RateLimitProperties.Rule rule = new RateLimitProperties.Rule();
		rule.setObserveOnly(false);
		rule.setThreshold(threshold);
		rule.setWindowSeconds(windowSeconds);
		rule.setLockSeconds(lockSeconds);
		properties.getRules().put(ruleId, rule);
	}

	// ---- no-op：未配置/未启用 ----

	@Test
	public void hit_returnsAllowAndSkipsRedis_whenNoRuleConfigured() {
		properties.setEnabled(true); // 启用但没有任何规则
		RateLimitDecision decision = service.hit("unknown-rule", "13800000000");

		assertThat(decision.isAllowed()).isTrue();
		assertThat(decision.isBlocked()).isFalse();
		// 无规则不应触碰 Redis，确保零业务影响
		verify(valueOps, never()).increment(anyString());
	}

	@Test
	public void hit_returnsAllowAndSkipsRedis_whenGloballyDisabled() {
		// enabled=false，即便配了规则也必须 no-op
		RateLimitProperties.Rule rule = new RateLimitProperties.Rule();
		rule.setThreshold(1);
		rule.setWindowSeconds(60);
		properties.getRules().put("sms-send", rule);

		RateLimitDecision decision = service.hit("sms-send", "13800000000");

		assertThat(decision.isAllowed()).isTrue();
		verify(valueOps, never()).increment(anyString());
	}

	// ---- 计数限流 ----

	@Test
	public void hit_allows_whenCountUnderThreshold() {
		enforceRule("sms-send", 5, 60, 0);
		when(valueOps.increment(anyString())).thenReturn(3L);

		RateLimitDecision decision = service.hit("sms-send", "13800000000");

		assertThat(decision.isAllowed()).isTrue();
		assertThat(decision.isBlocked()).isFalse();
		assertThat(decision.getCurrentCount()).isEqualTo(3L);
	}

	@Test
	public void hit_blocks_whenCountOverThresholdInEnforceMode() {
		enforceRule("sms-send", 5, 60, 0);
		when(valueOps.increment(anyString())).thenReturn(6L);

		RateLimitDecision decision = service.hit("sms-send", "13800000000");

		assertThat(decision.isAllowed()).isFalse();
		assertThat(decision.isBlocked()).isTrue();
		assertThat(decision.getErrorCode()).isEqualTo(429);
		assertThat(decision.getErrorMessage()).isEqualTo("请求过于频繁，请稍后再试");
	}

	@Test
	public void hit_setsExpire_whenKeyHasNoTtl() {
		enforceRule("sms-send", 5, 60, 0);
		when(valueOps.increment(anyString())).thenReturn(1L);
		// getExpire 返回 -1 表示 key 无过期（首次计数，或上次 EXPIRE 丢失）→ 应补设窗口 TTL
		when(redisTemplate.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(-1L);

		service.hit("sms-send", "13800000000");

		verify(redisTemplate).expire(anyString(), eq(60L), eq(TimeUnit.SECONDS));
	}

	@Test
	public void hit_selfHealsExpire_whenTtlLostOnSubsequentCount() {
		enforceRule("sms-send", 5, 60, 0);
		// 非首次计数（返回 2）但 TTL 丢失（-1）→ 仍应补设窗口 TTL，避免 key 永不过期被永久限流
		when(valueOps.increment(anyString())).thenReturn(2L);
		when(redisTemplate.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(-1L);

		service.hit("sms-send", "13800000000");

		verify(redisTemplate).expire(anyString(), eq(60L), eq(TimeUnit.SECONDS));
	}

	@Test
	public void hit_doesNotResetExpire_whenTtlStillValid() {
		enforceRule("sms-send", 5, 60, 0);
		// key 已有有效 TTL（剩 30 秒）→ 不应续期，否则窗口被无限续期、永不重置
		when(valueOps.increment(anyString())).thenReturn(2L);
		when(redisTemplate.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(30L);

		service.hit("sms-send", "13800000000");

		verify(redisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
	}

	// ---- observe-only ----

	@Test
	public void hit_marksButAllows_inObserveOnlyMode() {
		properties.setEnabled(true);
		RateLimitProperties.Rule rule = new RateLimitProperties.Rule();
		rule.setObserveOnly(true); // 观察模式
		rule.setThreshold(5);
		rule.setWindowSeconds(60);
		properties.getRules().put("sms-send", rule);
		when(valueOps.increment(anyString())).thenReturn(99L); // 远超阈值

		RateLimitDecision decision = service.hit("sms-send", "13800000000");

		// 观察模式：达到阻断条件 → blocked=true 供统计，但仍放行
		assertThat(decision.isBlocked()).isTrue();
		assertThat(decision.isObserveOnly()).isTrue();
		assertThat(decision.isAllowed()).isTrue();
	}

	// ---- fail-open ----

	@Test
	public void hit_failsOpen_whenRedisThrows() {
		enforceRule("sms-send", 5, 60, 0);
		when(valueOps.increment(anyString())).thenThrow(new RuntimeException("redis down"));

		RateLimitDecision decision = service.hit("sms-send", "13800000000");

		// Redis 故障不得阻断业务
		assertThat(decision.isAllowed()).isTrue();
		assertThat(decision.isBlocked()).isFalse();
	}

	// ---- 失败锁定 ----

	@Test
	public void recordFailure_writesLock_whenFailureCountReachesThreshold() {
		enforceRule("login-fail", 3, 600, 900);
		when(valueOps.increment(anyString())).thenReturn(3L); // 达到失败阈值

		RateLimitDecision decision = service.recordFailure("login-fail", "user-1");

		// 达到阈值即写锁定标记，TTL=lockSeconds
		verify(valueOps).set(anyString(), eq("1"), eq(900L), eq(TimeUnit.SECONDS));
		assertThat(decision.isBlocked()).isTrue();
	}

	@Test
	public void recordFailure_doesNotLock_inObserveOnlyMode() {
		// 观察模式即便失败达到阈值也不得写锁定标记，否则灰度期会真把用户锁死
		properties.setEnabled(true);
		RateLimitProperties.Rule rule = new RateLimitProperties.Rule();
		rule.setObserveOnly(true);
		rule.setThreshold(3);
		rule.setWindowSeconds(600);
		rule.setLockSeconds(900);
		properties.getRules().put("login-fail", rule);
		when(valueOps.increment(anyString())).thenReturn(5L); // 远超阈值

		RateLimitDecision decision = service.recordFailure("login-fail", "user-1");

		verify(valueOps, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
		assertThat(decision.isBlocked()).isTrue();   // 达到阻断条件，供统计
		assertThat(decision.isAllowed()).isTrue();   // 但观察模式仍放行
	}

	@Test
	public void recordFailure_doesNotLock_whenUnderThreshold() {
		enforceRule("login-fail", 3, 600, 900);
		when(valueOps.increment(anyString())).thenReturn(1L);

		RateLimitDecision decision = service.recordFailure("login-fail", "user-1");

		verify(valueOps, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
		assertThat(decision.isBlocked()).isFalse();
	}

	@Test
	public void isLocked_returnsTrue_whenLockKeyExists() {
		enforceRule("login-fail", 3, 600, 900);
		when(redisTemplate.hasKey(anyString())).thenReturn(true);

		assertThat(service.isLocked("login-fail", "user-1")).isTrue();
	}

	@Test
	public void isLocked_returnsFalse_whenNoRule() {
		// 无规则时不查 Redis，直接视为未锁定（no-op）
		assertThat(service.isLocked("nope", "user-1")).isFalse();
		verify(redisTemplate, never()).hasKey(anyString());
	}

	@Test
	public void isLocked_failsOpen_whenRedisThrows() {
		enforceRule("login-fail", 3, 600, 900);
		when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("redis down"));

		// fail-open：锁定检查故障时按“未锁定”放行
		assertThat(service.isLocked("login-fail", "user-1")).isFalse();
	}

	// ---- clear ----

	@Test
	public void clear_deletesCountAndLockKeys() {
		enforceRule("login-fail", 3, 600, 900);

		service.clear("login-fail", "user-1");

		// 计数 key 与锁定 key 都应被清除（如成功登录后清零失败计数与解锁），共两次 delete
		verify(redisTemplate, times(2)).delete(anyString());
	}

	// ---- PII 哈希 ----

	@Test
	public void redisKey_doesNotContainRawPii() {
		enforceRule("sms-send", 5, 60, 0);
		final String[] captured = new String[1];
		when(valueOps.increment(anyString())).thenAnswer(invocation -> {
			captured[0] = invocation.getArgument(0);
			return 1L;
		});

		String phone = "13800001111";
		service.hit("sms-send", phone);

		// key 必须包含前缀与 ruleId，但绝不能含明文手机号
		assertThat(captured[0]).startsWith("smart:security:rate-limit:sms-send:");
		assertThat(captured[0]).doesNotContain(phone);
	}
}
