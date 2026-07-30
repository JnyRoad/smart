package com.tce.smart.common.data.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * 使用 Redis 原子 {@code SET NX + TTL} 消费一次性 nonce。
 * <p>
 * 与互斥锁不同，证明 nonce 永不主动释放；其 TTL 到期后由证明自身的失效时间
 * 覆盖。Redis 不能确认写入时必须失败关闭，不能把重放保护降级成可选能力。
 */
@Slf4j
@Component
public class RedisNonceReplayGuard {

	private static final String KEY_PREFIX = "smart:security:legacy-compat:nonce:";
	private static final char[] HEX = "0123456789abcdef".toCharArray();

	private final StringRedisTemplate redisTemplate;

	public RedisNonceReplayGuard(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 原子预留 nonce。
	 *
	 * @return ACCEPTED 表示首次消费；REPLAYED 表示已被消费；UNAVAILABLE 表示 Redis
	 * 无法可靠确认，调用方必须返回服务不可用而非放行。
	 */
	public ReserveResult reserve(String keyId, String callerId, String nonce, long ttlSeconds) {
		if (isBlank(keyId) || isBlank(callerId) || isBlank(nonce) || ttlSeconds <= 0) {
			return ReserveResult.UNAVAILABLE;
		}
		try {
			Boolean reserved = redisTemplate.opsForValue().setIfAbsent(redisKey(keyId, callerId, nonce), "1",
					ttlSeconds, TimeUnit.SECONDS);
			if (Boolean.TRUE.equals(reserved)) {
				return ReserveResult.ACCEPTED;
			}
			return Boolean.FALSE.equals(reserved) ? ReserveResult.REPLAYED : ReserveResult.UNAVAILABLE;
		} catch (RuntimeException exception) {
			// 仅记录非敏感的 keyId/callerId，nonce 本身不能进入日志或 Redis key 明文。
			log.warn("遗留兼容 nonce 预留失败，keyId={}, callerId={}, exception={}", keyId, callerId,
					exception.getClass().getSimpleName());
			return ReserveResult.UNAVAILABLE;
		}
	}

	private String redisKey(String keyId, String callerId, String nonce) {
		return KEY_PREFIX + sha256(keyId + '\n' + callerId + '\n' + nonce);
	}

	private String sha256(String value) {
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			char[] output = new char[hash.length * 2];
			for (int index = 0; index < hash.length; index++) {
				int unsigned = hash[index] & 0xFF;
				output[index * 2] = HEX[unsigned >>> 4];
				output[index * 2 + 1] = HEX[unsigned & 0x0F];
			}
			return new String(output);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 算法不可用", exception);
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	public enum ReserveResult {
		ACCEPTED,
		REPLAYED,
		UNAVAILABLE
	}
}
