package com.tce.smart.schedule.service.comm.impl;

import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.schedule.constant.Constants;
import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.tool.enums.TimerTaskEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * description: SwitchServiceImpl <br>
 * date: 2020/1/15 15:49 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Service
@Slf4j
public class SwitchServiceImpl implements ISwitchService {
	private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
			"if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
			Long.class);
	/** 比较令牌后原子续租，令牌失配时不得延长其他实例的锁。 */
	private static final DefaultRedisScript<Long> RENEW_LOCK_SCRIPT = new DefaultRedisScript<>(
			"if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('pexpire', KEYS[1], ARGV[2]) else return 0 end",
			Long.class);
	private static final int RELEASE_LOCK_RETRY_TIMES = 2;
	private static final long RELEASE_LOCK_RETRY_SLEEP_MILLIS = 100L;

	@Autowired
	private StringRedisTemplate redisTemplate;


	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public Boolean process(TimerTaskEnum timerTask) {
		return process(timerTask, Constants.ONE, TimeUnit.MINUTES);
	}

	@Override
	public Boolean process(TimerTaskEnum timerTask, long timeout, TimeUnit timeUnit) {
		long lockTimeout = timeout > 0 ? timeout : Constants.ONE;
		TimeUnit lockTimeUnit = timeUnit == null ? TimeUnit.MINUTES : timeUnit;
		// setIfAbsent单命令原子抢占，避免hasKey+set窗口期多实例同时通过检查
		Boolean acquired = redisTemplate.opsForValue().setIfAbsent(timerTask.getKey(), timerTask.getDesc(),
				lockTimeout, lockTimeUnit);
		if (Boolean.TRUE.equals(acquired)) {
			log.info("开始执行：" + timerTask.getDesc() + " " + DateUtils.now());
			return true;
		}
		return false;
	}

	@Override
	public String acquire(TimerTaskEnum timerTask, long timeout, TimeUnit timeUnit) {
		long lockTimeout = timeout > 0 ? timeout : Constants.ONE;
		TimeUnit lockTimeUnit = timeUnit == null ? TimeUnit.MINUTES : timeUnit;
		String lockToken = timerTask.getDesc() + ":" + UUID.randomUUID();
		Boolean acquired = redisTemplate.opsForValue().setIfAbsent(timerTask.getKey(), lockToken,
				lockTimeout, lockTimeUnit);
		if (Boolean.TRUE.equals(acquired)) {
			log.info("开始执行：" + timerTask.getDesc() + " " + DateUtils.now());
			return lockToken;
		}
		return null;
	}

	@Override
	public void release(TimerTaskEnum timerTask, String lockToken) {
		if (timerTask == null || lockToken == null || lockToken.isEmpty()) {
			return;
		}
		for (int retry = 0; retry <= RELEASE_LOCK_RETRY_TIMES; retry++) {
			try {
				Long deleted = redisTemplate.execute(RELEASE_LOCK_SCRIPT, Collections.singletonList(timerTask.getKey()), lockToken);
				if (!Long.valueOf(1L).equals(deleted)) {
					log.info("定时任务锁已换主，跳过释放：" + timerTask.getDesc());
				}
				return;
			} catch (Exception e) {
				if (retry >= RELEASE_LOCK_RETRY_TIMES) {
					log.error("释放定时任务锁失败，等待TTL自动过期：{}", timerTask.getDesc(), e);
					return;
				}
				sleepBeforeReleaseRetry(timerTask);
			}
		}
	}

	@Override
	public boolean renew(TimerTaskEnum timerTask, String lockToken, long timeout, TimeUnit timeUnit) {
		if (timerTask == null || lockToken == null || lockToken.isEmpty() || timeout <= 0 || timeUnit == null) {
			return false;
		}
		long timeoutMillis = timeUnit.toMillis(timeout);
		if (timeoutMillis <= 0) {
			return false;
		}
		Long renewed = redisTemplate.execute(RENEW_LOCK_SCRIPT, Collections.singletonList(timerTask.getKey()), lockToken,
				String.valueOf(timeoutMillis));
		return Long.valueOf(1L).equals(renewed);
	}

	@Override
	public boolean isLocked(TimerTaskEnum timerTask) {
		return timerTask != null && Boolean.TRUE.equals(redisTemplate.hasKey(timerTask.getKey()));
	}

	private void sleepBeforeReleaseRetry(TimerTaskEnum timerTask) {
		try {
			TimeUnit.MILLISECONDS.sleep(RELEASE_LOCK_RETRY_SLEEP_MILLIS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("释放定时任务锁重试被中断：{}", timerTask.getDesc(), e);
		}
	}

	@Override
	public LocalDateTime saveOrGetKey(String key) {
		ValueOperations<String, String> value = redisTemplate.opsForValue();
		String time = value.get(key);
		if (Objects.nonNull(time)) {
			return LocalDateTime.parse(time, formatter);
		} else {
			//如果没有记录最开始时间 则以1970年为开始计算时间
			return LocalDateTime.of(1970, 1, 1, 0, 0, 0);
		}
	}

	@Override
	public void recordSyncTime(String key, LocalDateTime lastTime) {
		//数据处理完成后 更新最后时间
		String nowTime = formatter.format(lastTime);
		ValueOperations<String, String> value = redisTemplate.opsForValue();
		value.set(key, nowTime);
	}
}
