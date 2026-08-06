package com.tce.smart.schedule.task;

import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.tool.enums.TimerTaskEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 能耗投影远程调用期间的受控锁续租心跳，任务结束后必须关闭线程资源。
 */
@Slf4j
final class EnergyProjectionLockHeartbeat implements AutoCloseable {
	private static final long HEARTBEAT_INTERVAL_SECONDS = 60L;
	private final ISwitchService switchService;
	private final TimerTaskEnum taskLock;
	private final String taskLockToken;
	private final long taskLockMinutes;
	private final ScheduledExecutorService executorService;
	private final AtomicBoolean ownershipLost = new AtomicBoolean(false);
	private volatile LockLease executionLock;
	private ScheduledFuture<?> heartbeatFuture;

	EnergyProjectionLockHeartbeat(ISwitchService switchService, TimerTaskEnum taskLock, String taskLockToken,
			long taskLockMinutes) {
		this(switchService, taskLock, taskLockToken, taskLockMinutes, createExecutorService());
	}

	EnergyProjectionLockHeartbeat(ISwitchService switchService, TimerTaskEnum taskLock, String taskLockToken,
			long taskLockMinutes, ScheduledExecutorService executorService) {
		this.switchService = switchService;
		this.taskLock = taskLock;
		this.taskLockToken = taskLockToken;
		this.taskLockMinutes = taskLockMinutes;
		this.executorService = executorService;
	}

	/**
	 * 固定频率续租，不创建与任务生命周期脱离的后台线程。
	 */
	void start() {
		heartbeatFuture = executorService.scheduleAtFixedRate(this::renewLocks, HEARTBEAT_INTERVAL_SECONDS,
				HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
	}

	/**
	 * 分别续租任务专属锁和共享执行锁；任一失败都记录严重日志但不打断远程调用收尾。
	 */
	void renewLocks() {
		renewLock(taskLock, taskLockToken, taskLockMinutes, "任务专属锁");
		LockLease currentExecutionLock = executionLock;
		if (currentExecutionLock != null) {
			renewLock(currentExecutionLock.timerTask, currentExecutionLock.lockToken, currentExecutionLock.lockMinutes,
					"共享执行锁");
		}
	}

	/**
	 * 共享执行锁获取成功后再登记续租，避免等待期间错误续租不存在的锁。
	 */
	boolean registerExecutionLock(TimerTaskEnum timerTask, String lockToken, long lockMinutes) {
		if (ownershipLost.get()) {
			return false;
		}
		executionLock = new LockLease(timerTask, lockToken, lockMinutes);
		return true;
	}

	boolean isOwnershipLost() {
		return ownershipLost.get();
	}

	private void renewLock(TimerTaskEnum timerTask, String lockToken, long lockMinutes, String lockName) {
		try {
			if (!switchService.renew(timerTask, lockToken, lockMinutes, TimeUnit.MINUTES)) {
				ownershipLost.set(true);
				log.error("严重：能耗投影{}续租失败，已失去排他执行权；已发起的单个远调依靠平台DB幂等和租约收尾", lockName);
			}
		} catch (Exception e) {
			ownershipLost.set(true);
			log.error("严重：能耗投影{}续租异常，已失去排他执行权；已发起的单个远调依靠平台DB幂等和租约收尾", lockName, e);
		}
	}

	@Override
	public void close() {
		if (heartbeatFuture != null) {
			heartbeatFuture.cancel(false);
		}
		executorService.shutdownNow();
	}

	private static ScheduledExecutorService createExecutorService() {
		ThreadFactory threadFactory = runnable -> {
			Thread thread = new Thread(runnable, "energy-projection-lock-heartbeat");
			thread.setDaemon(true);
			return thread;
		};
		return Executors.newSingleThreadScheduledExecutor(threadFactory);
	}

	/**
	 * 共享执行锁的不可变续租信息，由心跳线程安全读取。
	 */
	private static class LockLease {
		private final TimerTaskEnum timerTask;
		private final String lockToken;
		private final long lockMinutes;

		private LockLease(TimerTaskEnum timerTask, String lockToken, long lockMinutes) {
			this.timerTask = timerTask;
			this.lockToken = lockToken;
			this.lockMinutes = lockMinutes;
		}
	}
}
