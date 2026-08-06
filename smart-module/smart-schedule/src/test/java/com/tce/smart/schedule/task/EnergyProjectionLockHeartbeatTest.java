package com.tce.smart.schedule.task;

import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.tool.enums.TimerTaskEnum;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 能耗投影长时间远程调用期间的锁续租测试。
 */
public class EnergyProjectionLockHeartbeatTest {

	@Test
	public void renewsBothOwnedLocksDuringLongRunningRemoteCallAndShutsDownHeartbeat() {
		ISwitchService switchService = Mockito.mock(ISwitchService.class);
		ScheduledExecutorService executorService = Mockito.mock(ScheduledExecutorService.class);
		Mockito.when(switchService.renew(TimerTaskEnum.ENERGY_PROJECTION_DAILY, "daily-token", 90L, TimeUnit.MINUTES))
				.thenReturn(Boolean.TRUE);
		Mockito.when(switchService.renew(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION, "execution-token", 90L,
				TimeUnit.MINUTES)).thenReturn(Boolean.TRUE);
		EnergyProjectionLockHeartbeat heartbeat = new EnergyProjectionLockHeartbeat(switchService,
				TimerTaskEnum.ENERGY_PROJECTION_DAILY, "daily-token", 90L, executorService);
		heartbeat.registerExecutionLock(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION, "execution-token", 90L);

		heartbeat.start();
		heartbeat.renewLocks();
		heartbeat.close();

		Mockito.verify(executorService).scheduleAtFixedRate(Mockito.any(Runnable.class), 60L, 60L, TimeUnit.SECONDS);
		Mockito.verify(switchService).renew(TimerTaskEnum.ENERGY_PROJECTION_DAILY, "daily-token", 90L, TimeUnit.MINUTES);
		Mockito.verify(switchService).renew(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION, "execution-token", 90L,
				TimeUnit.MINUTES);
		Mockito.verify(executorService).shutdownNow();
	}

	@Test
	public void renewFailureMarksOwnershipLost() {
		ISwitchService switchService = Mockito.mock(ISwitchService.class);
		ScheduledExecutorService executorService = Mockito.mock(ScheduledExecutorService.class);
		Mockito.when(switchService.renew(TimerTaskEnum.ENERGY_PROJECTION_DAILY, "daily-token", 90L, TimeUnit.MINUTES))
				.thenReturn(Boolean.FALSE);
		EnergyProjectionLockHeartbeat heartbeat = new EnergyProjectionLockHeartbeat(switchService,
				TimerTaskEnum.ENERGY_PROJECTION_DAILY, "daily-token", 90L, executorService);

		heartbeat.renewLocks();

		org.junit.Assert.assertTrue(heartbeat.isOwnershipLost());
	}

	@Test
	public void renewExceptionMarksOwnershipLost() {
		ISwitchService switchService = Mockito.mock(ISwitchService.class);
		ScheduledExecutorService executorService = Mockito.mock(ScheduledExecutorService.class);
		Mockito.when(switchService.renew(TimerTaskEnum.ENERGY_PROJECTION_DAILY, "daily-token", 90L, TimeUnit.MINUTES))
				.thenThrow(new RuntimeException("redis unavailable"));
		EnergyProjectionLockHeartbeat heartbeat = new EnergyProjectionLockHeartbeat(switchService,
				TimerTaskEnum.ENERGY_PROJECTION_DAILY, "daily-token", 90L, executorService);

		heartbeat.renewLocks();

		org.junit.Assert.assertTrue(heartbeat.isOwnershipLost());
	}
}
