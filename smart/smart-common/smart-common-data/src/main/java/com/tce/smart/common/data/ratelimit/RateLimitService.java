package com.tce.smart.common.data.ratelimit;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的通用限流服务。
 *
 * <p>提供两类能力：</p>
 * <ol>
 *   <li><b>计数限流</b>（{@link #hit}）：固定窗口内对某 key 累计请求次数，超阈即判定超限；</li>
 *   <li><b>失败锁定</b>（{@link #recordFailure}/{@link #isLocked}/{@link #clear}）：累计失败次数达阈值后
 *       写入一个带 TTL 的锁定标记，锁定期内 {@link #isLocked} 返回 true，常用于登录/验证码连续失败封禁。</li>
 * </ol>
 *
 * <p><b>设计原则</b>：</p>
 * <ul>
 *   <li><b>纯判定不副作用</b>：只返回 {@link RateLimitDecision}，是否真正拒绝由调用方决定；</li>
 *   <li><b>no-op 默认</b>：未启用或未命中规则时直接放行且不触碰 Redis，保证合并即零业务影响；</li>
 *   <li><b>fail-open</b>：Redis 异常时按规则的 failOpen 决定放行/拒绝，默认放行——限流自身故障不该误伤业务；</li>
 *   <li><b>PII 哈希</b>：手机号/证件号/openId 等敏感标识只以 MD5 进入 key，绝不明文落 Redis 或日志；</li>
 *   <li><b>泛化提示</b>：阻断文案统一为“请求过于频繁”，不暴露阈值等可被探测的策略细节。</li>
 * </ul>
 *
 * <p>本类是无状态线程安全单例，所有状态都在 Redis。</p>
 */
@Slf4j
public class RateLimitService {

	/**
	 * Redis key 统一前缀，便于运维按前缀检索/清理限流相关 key。
	 */
	private static final String KEY_PREFIX = "smart:security:rate-limit:";

	/**
	 * 失败锁定标记的子前缀，与计数 key 区分，避免相互覆盖。
	 */
	private static final String LOCK_INFIX = "lock:";

	/**
	 * key 分段分隔符。
	 */
	private static final char SEP = ':';

	private final StringRedisTemplate redisTemplate;
	private final RateLimitProperties properties;

	/**
	 * 可选的监控指标注册表。下游未引入 micrometer 时为 null，此时静默不上报，绝不报错。
	 */
	private final MeterRegistry meterRegistry;

	/**
	 * @param redisTemplate 字符串 Redis 操作模板（计数/锁定都用字符串值，避免序列化开销）
	 * @param properties    限流配置
	 * @param meterRegistry 可选指标注册表，允许为 null
	 */
	public RateLimitService(StringRedisTemplate redisTemplate,
							RateLimitProperties properties,
							ObjectProvider<MeterRegistry> meterRegistry) {
		this.redisTemplate = redisTemplate;
		this.properties = properties;
		// ObjectProvider 优雅取值：下游没有 MeterRegistry bean（或整个 ObjectProvider 为 null）时
		// 取到 null，运行期静默不上报指标，不抛异常、不强制下游引入 micrometer
		this.meterRegistry = meterRegistry == null ? null : meterRegistry.getIfAvailable();
	}

	/**
	 * 计数一次并给出限流决策。
	 *
	 * <p>固定窗口算法：对 key 执行 INCR，并确保 key 带 windowSeconds 的 TTL；窗口到期后 key 自动过期、
	 * 计数归零，实现窗口重置。</p>
	 *
	 * <p><b>阈值语义</b>：窗口内允许 threshold 次，即第 threshold 次仍放行，第 threshold+1 次（count &gt; threshold）
	 * 才判定超限。这与 {@link #recordFailure} 的“失败达到 threshold 次即锁定”语义不同，注意区分。</p>
	 *
	 * @param ruleId   规则 ID
	 * @param keyParts 业务维度标识（如手机号、IP、openId），会被归一化并对 PII 哈希后拼入 key
	 * @return 限流决策；未启用/无规则/异常 fail-open 时为放行
	 */
	public RateLimitDecision hit(String ruleId, String... keyParts) {
		RateLimitProperties.Rule rule = resolveRule(ruleId);
		if (rule == null || rule.getThreshold() <= 0) {
			// no-op：未启用、无规则或阈值无意义 → 直接放行，不触碰 Redis
			return RateLimitDecision.allow(ruleId, 0, 0, false);
		}

		String key = buildCountKey(ruleId, keyParts);
		long count;
		try {
			count = incrementWithWindow(key, rule.getWindowSeconds());
		} catch (Exception e) {
			return onRedisError(ruleId, rule, "hit", e);
		}

		boolean overThreshold = count > rule.getThreshold();
		if (!overThreshold) {
			record(ruleId, "hit", true, false, false);
			return RateLimitDecision.allow(ruleId, count, rule.getThreshold(), rule.isObserveOnly());
		}
		if (rule.isObserveOnly()) {
			// 观察模式：只标记不阻断，便于灰度期评估真实命中情况
			record(ruleId, "hit", true, true, true);
			return RateLimitDecision.observe(ruleId, count, rule.getThreshold());
		}
		// 强制模式：超阈阻断
		record(ruleId, "hit", false, true, false);
		return RateLimitDecision.block(ruleId, count, rule.getThreshold(),
			rule.getErrorCode(), rule.getErrorMessage());
	}

	/**
	 * 累计一次失败，达到阈值时写入失败锁定标记。
	 *
	 * <p>典型用法：登录/短信校验失败后调用本方法；后续在入口用 {@link #isLocked} 判断是否已被锁定；
	 * 成功后调用 {@link #clear} 清零失败计数并解锁。</p>
	 *
	 * <p><b>阈值语义</b>：失败计数达到（{@code >=}）threshold 即触发锁定，与 {@link #hit} 的“超过才限”不同。</p>
	 *
	 * @param ruleId   规则 ID
	 * @param keyParts 业务维度标识
	 * @return 决策：达到阈值时 blocked=true（observe-only 仍放行），否则放行
	 */
	public RateLimitDecision recordFailure(String ruleId, String... keyParts) {
		RateLimitProperties.Rule rule = resolveRule(ruleId);
		if (rule == null || rule.getThreshold() <= 0) {
			return RateLimitDecision.allow(ruleId, 0, 0, false);
		}

		String failKey = buildCountKey(ruleId, keyParts);
		long failCount;
		try {
			failCount = incrementWithWindow(failKey, rule.getWindowSeconds());
			boolean reachedThreshold = failCount >= rule.getThreshold();
			// 观察模式下绝不写锁定标记：observe-only 必须是纯观察，不能改变任何会被 isLocked 读到的状态，
			// 否则灰度期就会真把用户锁住，违背“合并即零业务影响”的约定。
			if (reachedThreshold && rule.getLockSeconds() > 0 && !rule.isObserveOnly()) {
				// 写锁定标记，TTL=lockSeconds，锁定期内 isLocked 返回 true
				String lockKey = buildLockKey(ruleId, keyParts);
				redisTemplate.opsForValue().set(lockKey, "1", rule.getLockSeconds(), TimeUnit.SECONDS);
			}
			if (reachedThreshold) {
				if (rule.isObserveOnly()) {
					record(ruleId, "failure", true, true, true);
					return RateLimitDecision.observe(ruleId, failCount, rule.getThreshold());
				}
				record(ruleId, "failure", false, true, false);
				return RateLimitDecision.block(ruleId, failCount, rule.getThreshold(),
					rule.getErrorCode(), rule.getErrorMessage());
			}
			record(ruleId, "failure", true, false, rule.isObserveOnly());
			return RateLimitDecision.allow(ruleId, failCount, rule.getThreshold(), rule.isObserveOnly());
		} catch (Exception e) {
			return onRedisError(ruleId, rule, "failure", e);
		}
	}

	/**
	 * 判断指定维度当前是否处于失败锁定中。
	 *
	 * @param ruleId   规则 ID
	 * @param keyParts 业务维度标识
	 * @return true 表示已锁定；无规则或 fail-open 异常兜底时返回 false（视为未锁定，放行）
	 */
	public boolean isLocked(String ruleId, String... keyParts) {
		RateLimitProperties.Rule rule = resolveRule(ruleId);
		if (rule == null) {
			return false;
		}
		String lockKey = buildLockKey(ruleId, keyParts);
		try {
			return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
		} catch (Exception e) {
			// fail-open：锁定检查故障不应把正常用户挡在门外
			log.warn("rate-limit isLocked redis error, fail-open: ruleId={}, error={}", ruleId, e.toString());
			record(ruleId, "isLocked", true, false, false);
			return false;
		}
	}

	/**
	 * 清除指定维度的计数与锁定标记（如成功登录后重置）。无规则时为 no-op。
	 *
	 * @param ruleId   规则 ID
	 * @param keyParts 业务维度标识
	 */
	public void clear(String ruleId, String... keyParts) {
		RateLimitProperties.Rule rule = resolveRule(ruleId);
		if (rule == null) {
			return;
		}
		try {
			redisTemplate.delete(buildCountKey(ruleId, keyParts));
			redisTemplate.delete(buildLockKey(ruleId, keyParts));
		} catch (Exception e) {
			// 清除失败仅记日志，不抛出——清除是“尽力而为”，失败不影响主流程
			log.warn("rate-limit clear redis error: ruleId={}, error={}", ruleId, e.toString());
		}
	}

	/**
	 * 解析规则：仅当总开关启用时才返回命中的规则，否则返回 null（触发 no-op）。
	 */
	private RateLimitProperties.Rule resolveRule(String ruleId) {
		if (!properties.isEnabled() || ruleId == null) {
			return null;
		}
		return properties.getRules().get(ruleId);
	}

	/**
	 * 对计数 key 执行 INCR，并确保它带有窗口 TTL。
	 *
	 * <p>固定窗口实现的经典坑：INCR 与 EXPIRE 非原子，若只在“首次计数”设 TTL，一旦那次 EXPIRE
	 * 因网络/崩溃丢失，key 将永不过期、计数无限累积，导致该维度被<b>永久限流</b>——这与 fail-open
	 * 精神相悖。这里改为“每次 INCR 后检查 TTL，发现无过期（getExpire &lt; 0）就补设”，使窗口在下一次
	 * 调用即可自愈，代价仅多一次轻量 TTL 查询。</p>
	 *
	 * @param key           计数 key
	 * @param windowSeconds 窗口秒数，{@code <= 0} 表示不设过期
	 * @return INCR 后的计数值
	 */
	private long incrementWithWindow(String key, long windowSeconds) {
		Long incremented = redisTemplate.opsForValue().increment(key);
		long count = incremented == null ? 0L : incremented;
		if (windowSeconds > 0) {
			Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
			// ttl 为 null（key 不存在，理论不该发生）或 < 0（-1 无过期 / -2 不存在）时补设窗口 TTL
			if (ttl == null || ttl < 0) {
				redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
			}
		}
		return count;
	}

	/**
	 * 构造计数 key：{@code smart:security:rate-limit:<ruleId>:<hash(keyParts)>}。
	 */
	private String buildCountKey(String ruleId, String... keyParts) {
		return KEY_PREFIX + ruleId + SEP + hashParts(keyParts);
	}

	/**
	 * 构造锁定 key：{@code smart:security:rate-limit:<ruleId>:lock:<hash(keyParts)>}。
	 */
	private String buildLockKey(String ruleId, String... keyParts) {
		return KEY_PREFIX + ruleId + SEP + LOCK_INFIX + hashParts(keyParts);
	}

	/**
	 * 归一化并哈希维度标识。
	 *
	 * <p>归一化：各段去首尾空白后用 {@code |} 连接，保证“同一逻辑标识”稳定映射到同一 key；
	 * 哈希：整体取 MD5，确保手机号/证件号/openId 等 PII 不以明文进入 Redis key 或日志。</p>
	 */
	private String hashParts(String... keyParts) {
		StringBuilder sb = new StringBuilder();
		if (keyParts != null) {
			for (int i = 0; i < keyParts.length; i++) {
				if (i > 0) {
					sb.append('|');
				}
				sb.append(keyParts[i] == null ? "" : keyParts[i].trim());
			}
		}
		return DigestUtils.md5DigestAsHex(sb.toString().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Redis 异常统一兜底：按规则 failOpen 决定放行/拒绝，并记日志与指标。
	 */
	private RateLimitDecision onRedisError(String ruleId, RateLimitProperties.Rule rule, String mode, Exception e) {
		log.warn("rate-limit redis error, failOpen={}: ruleId={}, mode={}, error={}",
			rule.isFailOpen(), ruleId, mode, e.toString());
		record(ruleId, mode, rule.isFailOpen(), !rule.isFailOpen(), false);
		if (rule.isFailOpen()) {
			return RateLimitDecision.allow(ruleId, 0, rule.getThreshold(), rule.isObserveOnly());
		}
		// fail-close：仅极高安全要求场景使用，宁可错杀
		return RateLimitDecision.block(ruleId, 0, rule.getThreshold(), rule.getErrorCode(), rule.getErrorMessage());
	}

	/**
	 * 统一观测：记结构化日志，并在有 MeterRegistry 时上报计数指标。
	 *
	 * @param ruleId  规则 ID
	 * @param mode    操作类型（hit/failure/isLocked）
	 * @param allowed 是否放行
	 * @param blocked 是否达到阻断条件
	 * @param observe 是否观察模式
	 */
	private void record(String ruleId, String mode, boolean allowed, boolean blocked, boolean observe) {
		if (log.isDebugEnabled()) {
			log.debug("rate-limit ruleId={}, mode={}, allowed={}, blocked={}, observeOnly={}",
				ruleId, mode, allowed, blocked, observe);
		}
		if (meterRegistry != null) {
			meterRegistry.counter("smart.security.rate_limit",
				"ruleId", ruleId,
				"mode", mode,
				"allowed", String.valueOf(allowed),
				"blocked", String.valueOf(blocked)).increment();
		}
	}
}
