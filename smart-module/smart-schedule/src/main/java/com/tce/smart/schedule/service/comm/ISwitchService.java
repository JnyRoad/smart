package com.tce.smart.schedule.service.comm;

import com.tce.smart.tool.enums.TimerTaskEnum;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * description: SwitchService <br>
 * date: 2020/1/15 15:40 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
public interface ISwitchService {

	Boolean process(TimerTaskEnum timerTask);

	Boolean process(TimerTaskEnum timerTask, long timeout, TimeUnit timeUnit);

	String acquire(TimerTaskEnum timerTask, long timeout, TimeUnit timeUnit);

	void release(TimerTaskEnum timerTask, String lockToken);

	LocalDateTime saveOrGetKey(String key);

	void recordSyncTime(String key, LocalDateTime lastTime);
}
