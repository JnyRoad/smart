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

	/**
	 * 仅当当前令牌仍持有锁时续租，避免延长已经被其他实例接管的锁。
	 */
	boolean renew(TimerTaskEnum timerTask, String lockToken, long timeout, TimeUnit timeUnit);

	/**
	 * 只读取指定任务锁是否存在，用于调度优先级让路，不承担互斥正确性。
	 */
	boolean isLocked(TimerTaskEnum timerTask);

	LocalDateTime saveOrGetKey(String key);

	void recordSyncTime(String key, LocalDateTime lastTime);
}
