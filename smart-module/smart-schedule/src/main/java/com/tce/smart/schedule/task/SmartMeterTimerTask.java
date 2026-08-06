package com.tce.smart.schedule.task;

import cn.hutool.core.date.DateUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.feign.RemoteDailySettlementService;
import com.tce.smart.platform.api.feign.RemoteEnergyProjectionService;
import com.tce.smart.schedule.config.TaskJob;
import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.schedule.service.platform.SmartMeterService;
import com.tce.smart.tool.enums.TimerTaskEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * 智能水电表定时任务
 *
 * @author sunfujian
 * @since 2021/11/16 15:39
 */
@Slf4j
@Component
@EnableScheduling
public class SmartMeterTimerTask {
	private static final long ENERGY_PROJECTION_PROCESS_PENDING_LOCK_MINUTES = 30L;
	private static final long ENERGY_PROJECTION_DAILY_LOCK_MINUTES = 90L;
	/** 所有能耗投影写入共用此锁，避免不同任务同时改写同一园区业务日。 */
	private static final long ENERGY_PROJECTION_EXECUTION_LOCK_MINUTES = 90L;

	@Autowired
	private ISwitchService iSwitchService;
	@Autowired
	private SmartMeterService smartMeterService;
	@Autowired
	private RemoteDailySettlementService remoteDailySettlementService;
	@Autowired
	private RemoteEnergyProjectionService remoteEnergyProjectionService;
	@Autowired
	private TaskJob taskJob;
	@Value("${smart.energy.zone-id:Asia/Shanghai}")
	private String energyZoneId;
	@Value("${task.energy.projection.execution-wait-seconds:7200}")
	private long energyProjectionExecutionWaitSeconds = 7200L;

	/**
	 * 定时查询设备连接状态
	 * 每5分钟同步一次
	 */
	@Scheduled(cron = "${task.meter.checkOnline}")
	public void deviceStatusTask() {
		if (taskJob.getSmartMeterStatus() && iSwitchService.process(TimerTaskEnum.SMART_METER_STATUS)) {
			try {
				log.info("发起智能水电表状态查询");
				smartMeterService.queryDeviceStatus();
			} catch (Exception e) {
				log.error("查询设备状态异常", e);
			}
		}
	}

	/**
	 * 水表读数查询任务
	 * 每天凌晨1点执行
	 */
	@Scheduled(cron = "${task.meter.water.reading}")
	public void waterMeterReadingTask() {
		if (taskJob.getWaterMeterReading() && iSwitchService.process(TimerTaskEnum.WATER_METER_READING)) {
			try {
				log.info("水表读数查询任务开始,{}", DateUtil.now());
				smartMeterService.readWaterMeterValue();
			} catch (Exception e) {
				log.error("水表读数查询任务执行异常", e);
			}
		}
	}

	/**
	 * 电表读数查询任务
	 * 每天凌晨1点执行
	 */
	@Scheduled(cron = "${task.meter.ele.reading}")
	public void eleMeterReadingTask() {
		if (taskJob.getEleMeterReading() && iSwitchService.process(TimerTaskEnum.ELE_METER_READING)) {
			try {
				log.info("电表读数查询任务开始,{}", DateUtil.now());
				smartMeterService.readEleMeterValue();
				smartMeterService.readEleMeterState();
			} catch (Exception e) {
				log.error("电表读数查询任务执行异常", e);
			}
		}
	}

	/**
	 * 水电日结算
	 */
	@Scheduled(cron = "${task.meter.day.calc}")
	public void genDailySettlementRecord() {
		if (taskJob.getGenSettlementDaily() && iSwitchService.process(TimerTaskEnum.GEN_SETTLEMENT_DAILY)) {
			try {
				log.info("水电日结算开始,{}", DateUtil.now());
				remoteDailySettlementService.genDailyRecord(SecurityConstants.FROM_IN);
			} catch (Exception e) {
				log.error("水电日结算执行异常", e);
			}
		}
	}

	/**
	 * 处理能耗投影待处理队列。
	 */
	@Scheduled(cron = "${task.energy.projection.pending-cron:0 0/5 * * * ?}", zone = "${smart.energy.zone-id:Asia/Shanghai}")
	public void energyProjectionProcessPendingTask() {
		if (!Boolean.TRUE.equals(taskJob.getEnergyProjectionProcessPending())) {
			return;
		}
		try {
			if (iSwitchService.isLocked(TimerTaskEnum.ENERGY_PROJECTION_DAILY)) {
				log.info("能耗投影每日任务已排队或执行，待处理队列任务本次让路");
				return;
			}
		} catch (Exception e) {
			log.error("待处理队列任务读取每日任务锁状态异常，本次安全跳过", e);
			return;
		}
		executeEnergyProjectionTask(TimerTaskEnum.ENERGY_PROJECTION_PROCESS_PENDING,
				ENERGY_PROJECTION_PROCESS_PENDING_LOCK_MINUTES, false, false, "能耗投影待处理队列",
				heartbeat -> executeRemoteEnergyProjection("能耗投影待处理队列", heartbeat,
						() -> remoteEnergyProjectionService.processPending(SecurityConstants.FROM_IN,
								SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)));
	}

	/**
	 * 在每日临界区内调用平台单一入口，由平台按开关参数执行前一业务日回算和本月补齐。
	 */
	@Scheduled(cron = "${task.energy.projection.reconcile-cron:0 15 2 * * ?}", zone = "${smart.energy.zone-id:Asia/Shanghai}")
	public void energyProjectionDailyTask() {
		boolean reconcile = Boolean.TRUE.equals(taskJob.getEnergyProjectionReconcile());
		boolean backfill = Boolean.TRUE.equals(taskJob.getEnergyProjectionBackfill());
		if (!reconcile && !backfill) {
			return;
		}
		executeEnergyProjectionTask(TimerTaskEnum.ENERGY_PROJECTION_DAILY, ENERGY_PROJECTION_DAILY_LOCK_MINUTES,
				true, true, "能耗投影每日串行编排", heartbeat -> executeRemoteEnergyProjection("能耗投影每日编排", heartbeat,
						() -> remoteEnergyProjectionService.daily(
								LocalDate.now(ZoneId.of(energyZoneId)).minusDays(1).toString(), reconcile, backfill,
								SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)));
	}

	/**
	 * 先获取任务专属锁，再获取能耗投影全局执行锁，确保三类投影写入串行。
	 */
	private void executeEnergyProjectionTask(TimerTaskEnum taskLock, long taskLockMinutes, boolean waitForExecutionLock,
			boolean allowDailyTimeoutDegrade, String taskName, EnergyProjectionTask task) {
		String taskLockToken;
		try {
			taskLockToken = iSwitchService.acquire(taskLock, taskLockMinutes, TimeUnit.MINUTES);
		} catch (Exception e) {
			log.error("{}获取任务专属锁异常，本次安全跳过", taskName, e);
			return;
		}
		if (taskLockToken == null) {
			log.info("{}未获取任务专属锁，本次跳过", taskName);
			return;
		}

		String executionLockToken = null;
		EnergyProjectionLockHeartbeat lockHeartbeat = null;
		try {
			lockHeartbeat = createEnergyProjectionLockHeartbeat(iSwitchService, taskLock, taskLockToken, taskLockMinutes);
			lockHeartbeat.start();
			ExecutionLockWaitResult executionLockResult = waitForExecutionLock
					? acquireExecutionLockWithWait(lockHeartbeat, taskName)
					: ExecutionLockWaitResult.acquired(iSwitchService.acquire(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION,
							ENERGY_PROJECTION_EXECUTION_LOCK_MINUTES, TimeUnit.MINUTES));
			executionLockToken = executionLockResult.lockToken;
			if (executionLockToken == null) {
				if (allowDailyTimeoutDegrade && executionLockResult.timedOut) {
					// 超时降级前立即以令牌续租确认 DAILY 锁仍归当前任务所有。
					lockHeartbeat.renewLocks();
				}
				if (allowDailyTimeoutDegrade && executionLockResult.timedOut && !lockHeartbeat.isOwnershipLost()
						&& !Thread.currentThread().isInterrupted()) {
					log.warn("{}等待共享执行锁超时，但仍持有DAILY锁，降级为无共享锁单次daily调用；平台持久化锁负责正确性",
							taskName);
					task.execute(lockHeartbeat);
					return;
				}
				if (!waitForExecutionLock) {
					log.info("{}未获取能耗投影共享执行锁，本次跳过", taskName);
				}
				return;
			}
			if (!lockHeartbeat.registerExecutionLock(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION, executionLockToken,
					ENERGY_PROJECTION_EXECUTION_LOCK_MINUTES)) {
				log.error("{}等待共享执行锁期间已失去任务锁所有权，禁止发起远程调用", taskName);
				return;
			}
			task.execute(lockHeartbeat);
		} catch (Exception e) {
			log.error("{}编排执行异常", taskName, e);
		} finally {
			if (lockHeartbeat != null) {
				lockHeartbeat.close();
			}
			if (executionLockToken != null) {
				iSwitchService.release(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION, executionLockToken);
			}
			iSwitchService.release(taskLock, taskLockToken);
		}
	}

	/**
	 * 每日任务已持有专属锁时有界等待共享执行锁，等待期间由心跳续租专属锁。
	 */
	private ExecutionLockWaitResult acquireExecutionLockWithWait(EnergyProjectionLockHeartbeat lockHeartbeat,
			String taskName) {
		long waitSeconds = Math.max(0L, energyProjectionExecutionWaitSeconds);
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(waitSeconds);
		boolean waitingLogged = false;
		while (!lockHeartbeat.isOwnershipLost()) {
			if (Thread.currentThread().isInterrupted()) {
				log.warn("{}等待共享执行锁时线程已中断，本次安全退出", taskName);
				return ExecutionLockWaitResult.stopped();
			}
			String executionLockToken = iSwitchService.acquire(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION,
					ENERGY_PROJECTION_EXECUTION_LOCK_MINUTES, TimeUnit.MINUTES);
			if (executionLockToken != null) {
				return ExecutionLockWaitResult.acquired(executionLockToken);
			}
			long remainingNanos = deadline - System.nanoTime();
			if (remainingNanos <= 0) {
				log.warn("{}等待共享执行锁超时{}秒", taskName, waitSeconds);
				return ExecutionLockWaitResult.timedOut();
			}
			if (!waitingLogged) {
				log.info("{}等待能耗投影共享执行锁，最长等待{}秒", taskName, waitSeconds);
				waitingLogged = true;
			}
			long waitMillis = Math.min(5000L, Math.max(1L, TimeUnit.NANOSECONDS.toMillis(remainingNanos)));
			if (!waitBeforeExecutionLockRetry(waitMillis)) {
				return ExecutionLockWaitResult.stopped();
			}
		}
		log.error("{}等待共享执行锁期间已失去任务锁所有权，禁止发起远程调用", taskName);
		return ExecutionLockWaitResult.stopped();
	}

	/**
	 * 单次等待不超过五秒；中断时恢复中断标记并结束本次每日编排。
	 */
	protected boolean waitBeforeExecutionLockRetry(long waitMillis) {
		try {
			TimeUnit.MILLISECONDS.sleep(waitMillis);
			return true;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("等待能耗投影共享执行锁被中断，本次安全退出", e);
			return false;
		}
	}

	/**
	 * 为已获取的任务专属锁创建续租心跳，测试可替换为受控执行器。
	 */
	protected EnergyProjectionLockHeartbeat createEnergyProjectionLockHeartbeat(ISwitchService switchService,
			TimerTaskEnum taskLock, String taskLockToken, long taskLockMinutes) {
		return new EnergyProjectionLockHeartbeat(switchService, taskLock, taskLockToken, taskLockMinutes);
	}

	/**
	 * 单次 Feign 调用失败只记录日志，避免影响定时线程后续调度。
	 */
	private void executeRemoteEnergyProjection(String taskName, EnergyProjectionLockHeartbeat lockHeartbeat,
			EnergyProjectionRemoteOperation operation) {
		if (lockHeartbeat.isOwnershipLost()) {
			log.error("{}已失去锁所有权，禁止发起远程调用；已发起的单个远调依靠平台DB幂等和租约收尾", taskName);
			return;
		}
		try {
			Result<Boolean> result = operation.execute();
			if (result == null || !result.isSuccess() || !Boolean.TRUE.equals(result.getData())) {
				log.error("{}远程执行失败，result={}", taskName, result);
			}
		} catch (Exception e) {
			log.error("{}远程执行异常", taskName, e);
		}
	}

	/**
	 * 封装持锁期间执行的编排动作。
	 */
	private interface EnergyProjectionTask {
		void execute(EnergyProjectionLockHeartbeat lockHeartbeat);
	}

	/**
	 * 封装返回统一结果的 Feign 调用。
	 */
	private interface EnergyProjectionRemoteOperation {
		Result<Boolean> execute();
	}

	/**
	 * 区分共享锁已取得、明确超时和安全停止，避免把失权或中断误判为可降级超时。
	 */
	private static class ExecutionLockWaitResult {
		private final String lockToken;
		private final boolean timedOut;

		private ExecutionLockWaitResult(String lockToken, boolean timedOut) {
			this.lockToken = lockToken;
			this.timedOut = timedOut;
		}

		private static ExecutionLockWaitResult acquired(String lockToken) {
			return new ExecutionLockWaitResult(lockToken, false);
		}

		private static ExecutionLockWaitResult timedOut() {
			return new ExecutionLockWaitResult(null, true);
		}

		private static ExecutionLockWaitResult stopped() {
			return new ExecutionLockWaitResult(null, false);
		}
	}
}
