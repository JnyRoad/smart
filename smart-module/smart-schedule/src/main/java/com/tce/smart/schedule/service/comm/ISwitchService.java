package com.tce.smart.schedule.service.comm;

import com.tce.smart.tool.enums.TimerTaskEnum;

import java.time.LocalDateTime;

/**
 * description: SwitchService <br>
 * date: 2020/1/15 15:40 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
public interface ISwitchService {

	Boolean process(TimerTaskEnum timerTask);

	LocalDateTime saveOrGetKey(String key);

	void recordSyncTime(String key, LocalDateTime lastTime);
}
