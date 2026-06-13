package com.tce.smart.schedule.service.comm.impl;

import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.schedule.constant.Constants;
import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.tool.enums.TimerTaskEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
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

	@Autowired
	private StringRedisTemplate redisTemplate;


	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public Boolean process(TimerTaskEnum timerTask) {
		// setIfAbsent单命令原子抢占，避免hasKey+set窗口期多实例同时通过检查
		Boolean acquired = redisTemplate.opsForValue().setIfAbsent(timerTask.getKey(), timerTask.getDesc(),
				Constants.ONE, TimeUnit.MINUTES);
		if (Boolean.TRUE.equals(acquired)) {
			log.info("开始执行：" + timerTask.getDesc() + " " + DateUtils.now());
			return true;
		}
		return false;
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
