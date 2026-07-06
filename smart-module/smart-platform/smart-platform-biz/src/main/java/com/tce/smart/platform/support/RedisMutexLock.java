package com.tce.smart.platform.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 通用 raw-key Redis 互斥锁（token + setIfAbsent + Lua 原子释放）。
 * 与 smart-schedule 的 ISwitchService 同模式，但不绑定 TimerTaskEnum，可按任意 key 加锁。
 * 注意：TTL 到期自动失效，调用方必须保证 TTL 大于临界区耗时上界（见 spec §3.2.2 终审 High 条目）。
 */
@Slf4j
@Component
public class RedisMutexLock {

	/** token 匹配才删除，避免误删他人锁 */
	private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
			"if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
			Long.class);

	private final StringRedisTemplate redisTemplate;

	public RedisMutexLock(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 尝试抢占锁。
	 * @return 成功返回持锁 token；已被占用返回 null
	 */
	public String acquire(String key, long ttlSeconds) {
		String token = UUID.randomUUID().toString();
		Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, token, ttlSeconds, TimeUnit.SECONDS);
		return Boolean.TRUE.equals(ok) ? token : null;
	}

	/** 原子释放：仅当 value 与 token 一致时删除 */
	public void release(String key, String token) {
		if (token == null || token.isEmpty()) {
			return;
		}
		try {
			redisTemplate.execute(RELEASE_SCRIPT, Collections.singletonList(key), token);
		} catch (Exception e) {
			log.error("释放互斥锁失败，等待TTL自动过期：key={}", key, e);
		}
	}
}
