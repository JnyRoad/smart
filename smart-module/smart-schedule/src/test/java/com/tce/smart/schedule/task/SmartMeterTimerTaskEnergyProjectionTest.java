package com.tce.smart.schedule.task;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.feign.RemoteEnergyProjectionService;
import com.tce.smart.schedule.config.TaskJob;
import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.tool.enums.TimerTaskEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * 能耗投影定时任务的分布式锁和内部调用契约测试。
 */
public class SmartMeterTimerTaskEnergyProjectionTest {

	@Test
	public void pendingDoesNothingWhenDisabled() throws Exception {
		TaskJob taskJob = new TaskJob();
		ISwitchService switchService = Mockito.mock(ISwitchService.class);
		RemoteEnergyProjectionService remoteService = Mockito.mock(RemoteEnergyProjectionService.class);
		SmartMeterTimerTask task = createTask(taskJob, switchService, remoteService);

		task.energyProjectionProcessPendingTask();

		Assert.assertEquals(Boolean.FALSE, taskJob.getEnergyProjectionProcessPending());
		Mockito.verifyZeroInteractions(switchService, remoteService);
	}

	@Test
	public void pendingDoesNotCallRemoteWhenTaskLockIsUnavailable() throws Exception {
		TaskJob taskJob = enabledTaskJob();
		ISwitchService switchService = Mockito.mock(ISwitchService.class);
		RemoteEnergyProjectionService remoteService = Mockito.mock(RemoteEnergyProjectionService.class);
		SmartMeterTimerTask task = createTask(taskJob, switchService, remoteService);
		Mockito.when(switchService.acquire(TimerTaskEnum.ENERGY_PROJECTION_PROCESS_PENDING, 30L, TimeUnit.MINUTES))
				.thenReturn(null);

		task.energyProjectionProcessPendingTask();

		Mockito.verify(switchService).acquire(TimerTaskEnum.ENERGY_PROJECTION_PROCESS_PENDING, 30L, TimeUnit.MINUTES);
		Mockito.verify(switchService, Mockito.never()).acquire(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION, 90L, TimeUnit.MINUTES);
		Mockito.verifyZeroInteractions(remoteService);
		Mockito.verify(switchService, Mockito.never()).release(Mockito.any(TimerTaskEnum.class), Mockito.anyString());
	}

	@Test
	public void pendingYieldsToAnAlreadyScheduledDailyTask() throws Exception {
		TaskJob taskJob = enabledTaskJob();
		ISwitchService switchService = Mockito.mock(ISwitchService.class);
		RemoteEnergyProjectionService remoteService = Mockito.mock(RemoteEnergyProjectionService.class);
		SmartMeterTimerTask task = createTask(taskJob, switchService, remoteService);
		Mockito.when(switchService.isLocked(TimerTaskEnum.ENERGY_PROJECTION_DAILY)).thenReturn(Boolean.TRUE);

		task.energyProjectionProcessPendingTask();

		Mockito.verify(switchService).isLocked(TimerTaskEnum.ENERGY_PROJECTION_DAILY);
		Mockito.verify(switchService, Mockito.never()).acquire(TimerTaskEnum.ENERGY_PROJECTION_PROCESS_PENDING,
				30L, TimeUnit.MINUTES);
		Mockito.verifyZeroInteractions(remoteService);
	}

	@Test
	public void dailyTimeoutDegradesToSingleDailyEndpointWhileDailyLockIsOwned() throws Exception {
		TaskJob taskJob = enabledTaskJob();
		ISwitchService switchService = Mockito.mock(ISwitchService.class);
		RemoteEnergyProjectionService remoteService = Mockito.mock(RemoteEnergyProjectionService.class);
		SmartMeterTimerTask task = createTask(taskJob, switchService, remoteService);
		Mockito.when(switchService.acquire(TimerTaskEnum.ENERGY_PROJECTION_DAILY, 90L, TimeUnit.MINUTES))
				.thenReturn("daily-token");
		Mockito.when(switchService.acquire(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION, 90L, TimeUnit.MINUTES))
				.thenReturn(null);
		Mockito.when(switchService.renew(TimerTaskEnum.ENERGY_PROJECTION_DAILY, "daily-token", 90L, TimeUnit.MINUTES))
				.thenReturn(Boolean.TRUE);

		String expectedBusinessDate = LocalDate.now(ZoneId.of("Asia/Shanghai")).minusDays(1).toString();
		Mockito.when(remoteService.daily(expectedBusinessDate, true, true, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)).thenReturn(Result.success(Boolean.TRUE));

		task.energyProjectionDailyTask();

		Mockito.verify(remoteService).daily(expectedBusinessDate, true, true, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		Mockito.verify(switchService).release(TimerTaskEnum.ENERGY_PROJECTION_DAILY, "daily-token");
		Mockito.verify(switchService, Mockito.never()).release(Mockito.eq(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION),
				Mockito.anyString());
	}

	@Test
	public void dailyTaskCallsSingleDailyEndpointWithCurrentSwitchesAndHeaders() throws Exception {
		TaskJob taskJob = enabledTaskJob();
		ISwitchService switchService = Mockito.mock(ISwitchService.class);
		RemoteEnergyProjectionService remoteService = Mockito.mock(RemoteEnergyProjectionService.class);
		SmartMeterTimerTask task = createTask(taskJob, switchService, remoteService);
		String expectedBusinessDate = LocalDate.now(ZoneId.of("Asia/Shanghai")).minusDays(1).toString();
		Mockito.when(switchService.acquire(TimerTaskEnum.ENERGY_PROJECTION_DAILY, 90L, TimeUnit.MINUTES))
				.thenReturn("daily-token");
		Mockito.when(switchService.acquire(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION, 90L, TimeUnit.MINUTES))
				.thenReturn("execution-token");
		Mockito.when(remoteService.daily(expectedBusinessDate, true, true, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)).thenReturn(Result.success(Boolean.TRUE));

		task.energyProjectionDailyTask();

		Mockito.verify(remoteService).daily(expectedBusinessDate, true, true, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		Mockito.verify(remoteService, Mockito.never()).reconcile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Mockito.verify(remoteService, Mockito.never()).backfillMonthToDate(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(switchService).release(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION, "execution-token");
		Mockito.verify(switchService).release(TimerTaskEnum.ENERGY_PROJECTION_DAILY, "daily-token");
		Mockito.verify(switchService, Mockito.times(1)).acquire(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION,
				90L, TimeUnit.MINUTES);
	}

	@Test
	public void dailyTaskWaitsForExecutionLockThenCallsSingleDailyEndpoint() throws Exception {
		TaskJob taskJob = enabledTaskJob();
		ISwitchService switchService = Mockito.mock(ISwitchService.class);
		RemoteEnergyProjectionService remoteService = Mockito.mock(RemoteEnergyProjectionService.class);
		HeartbeatCapturingSmartMeterTimerTask task = createHeartbeatCapturingTask(taskJob, switchService, remoteService);
		String expectedBusinessDate = LocalDate.now(ZoneId.of("Asia/Shanghai")).minusDays(1).toString();
		setField(task, "energyProjectionExecutionWaitSeconds", 1L);
		Mockito.when(switchService.acquire(TimerTaskEnum.ENERGY_PROJECTION_DAILY, 90L, TimeUnit.MINUTES))
				.thenReturn("daily-token");
		Mockito.when(switchService.acquire(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION, 90L, TimeUnit.MINUTES))
				.thenReturn(null, "execution-token");
		Mockito.when(remoteService.daily(expectedBusinessDate, true, true, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)).thenReturn(Result.success(Boolean.TRUE));

		task.energyProjectionDailyTask();

		Mockito.verify(switchService, Mockito.times(2)).acquire(TimerTaskEnum.ENERGY_PROJECTION_EXECUTION,
				90L, TimeUnit.MINUTES);
		Mockito.verify(remoteService).daily(expectedBusinessDate, true, true, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@Test
	public void dailyDoesNothingWhenBothSwitchesAreDisabled() throws Exception {
		TaskJob taskJob = new TaskJob();
		ISwitchService switchService = Mockito.mock(ISwitchService.class);
		RemoteEnergyProjectionService remoteService = Mockito.mock(RemoteEnergyProjectionService.class);
		SmartMeterTimerTask task = createTask(taskJob, switchService, remoteService);

		task.energyProjectionDailyTask();

		Mockito.verifyZeroInteractions(switchService, remoteService);
	}

	@Test
	public void dailyDoesNotCallRemoteWhenHeartbeatAlreadyLostOwnership() throws Exception {
		TaskJob taskJob = enabledTaskJob();
		ISwitchService switchService = Mockito.mock(ISwitchService.class);
		RemoteEnergyProjectionService remoteService = Mockito.mock(RemoteEnergyProjectionService.class);
		OwnershipLostSmartMeterTimerTask task = createOwnershipLostTask(taskJob, switchService, remoteService);
		Mockito.when(switchService.acquire(TimerTaskEnum.ENERGY_PROJECTION_DAILY, 90L, TimeUnit.MINUTES))
				.thenReturn("daily-token");
		Mockito.when(switchService.renew(TimerTaskEnum.ENERGY_PROJECTION_DAILY, "daily-token", 90L, TimeUnit.MINUTES))
				.thenReturn(Boolean.FALSE);

		task.energyProjectionDailyTask();

		Mockito.verifyZeroInteractions(remoteService);
	}

	@Test
	public void energyProjectionScheduledTasksUseShanghaiZoneAndExpectedCronDefaults() throws Exception {
		assertScheduled("energyProjectionProcessPendingTask", "${task.energy.projection.pending-cron:0 0/5 * * * ?}");
		assertScheduled("energyProjectionDailyTask", "${task.energy.projection.reconcile-cron:0 15 2 * * ?}");
	}

	private TaskJob enabledTaskJob() {
		TaskJob taskJob = new TaskJob();
		taskJob.setEnergyProjectionProcessPending(Boolean.TRUE);
		taskJob.setEnergyProjectionReconcile(Boolean.TRUE);
		taskJob.setEnergyProjectionBackfill(Boolean.TRUE);
		return taskJob;
	}

	private SmartMeterTimerTask createTask(TaskJob taskJob, ISwitchService switchService,
			RemoteEnergyProjectionService remoteService) throws Exception {
		SmartMeterTimerTask task = new SmartMeterTimerTask();
		setField(task, "taskJob", taskJob);
		setField(task, "iSwitchService", switchService);
		setField(task, "remoteEnergyProjectionService", remoteService);
		setField(task, "energyZoneId", "Asia/Shanghai");
		setField(task, "energyProjectionExecutionWaitSeconds", 0L);
		return task;
	}

	private HeartbeatCapturingSmartMeterTimerTask createHeartbeatCapturingTask(TaskJob taskJob,
			ISwitchService switchService, RemoteEnergyProjectionService remoteService) throws Exception {
		HeartbeatCapturingSmartMeterTimerTask task = new HeartbeatCapturingSmartMeterTimerTask();
		setField(task, "taskJob", taskJob);
		setField(task, "iSwitchService", switchService);
		setField(task, "remoteEnergyProjectionService", remoteService);
		setField(task, "energyZoneId", "Asia/Shanghai");
		setField(task, "energyProjectionExecutionWaitSeconds", 0L);
		return task;
	}

	private OwnershipLostSmartMeterTimerTask createOwnershipLostTask(TaskJob taskJob, ISwitchService switchService,
			RemoteEnergyProjectionService remoteService) throws Exception {
		OwnershipLostSmartMeterTimerTask task = new OwnershipLostSmartMeterTimerTask();
		setField(task, "taskJob", taskJob);
		setField(task, "iSwitchService", switchService);
		setField(task, "remoteEnergyProjectionService", remoteService);
		setField(task, "energyZoneId", "Asia/Shanghai");
		setField(task, "energyProjectionExecutionWaitSeconds", 0L);
		return task;
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Field field = findField(target.getClass(), name);
		field.setAccessible(true);
		field.set(target, value);
	}

	/**
	 * 测试子类复用父类私有依赖时，逐级定位字段，避免继承层级导致注入失败。
	 */
	private Field findField(Class<?> type, String name) throws NoSuchFieldException {
		Class<?> currentType = type;
		while (currentType != null) {
			try {
				return currentType.getDeclaredField(name);
			} catch (NoSuchFieldException ignored) {
				currentType = currentType.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}

	private void assertScheduled(String methodName, String expectedCron) throws Exception {
		Method method = SmartMeterTimerTask.class.getDeclaredMethod(methodName);
		Scheduled scheduled = method.getAnnotation(Scheduled.class);
		Assert.assertNotNull(scheduled);
		Assert.assertEquals(expectedCron, scheduled.cron());
		Assert.assertEquals("${smart.energy.zone-id:Asia/Shanghai}", scheduled.zone());
	}

	private static class HeartbeatCapturingSmartMeterTimerTask extends SmartMeterTimerTask {
		private EnergyProjectionLockHeartbeat lockHeartbeat;

		@Override
		protected EnergyProjectionLockHeartbeat createEnergyProjectionLockHeartbeat(ISwitchService switchService,
				TimerTaskEnum taskLock, String taskLockToken, long taskLockMinutes) {
			lockHeartbeat = new EnergyProjectionLockHeartbeat(switchService, taskLock, taskLockToken, taskLockMinutes,
					Mockito.mock(java.util.concurrent.ScheduledExecutorService.class));
			return lockHeartbeat;
		}

		@Override
		protected boolean waitBeforeExecutionLockRetry(long waitMillis) {
			return true;
		}
	}

	private static class OwnershipLostSmartMeterTimerTask extends HeartbeatCapturingSmartMeterTimerTask {
		@Override
		protected EnergyProjectionLockHeartbeat createEnergyProjectionLockHeartbeat(ISwitchService switchService,
				TimerTaskEnum taskLock, String taskLockToken, long taskLockMinutes) {
			EnergyProjectionLockHeartbeat heartbeat = super.createEnergyProjectionLockHeartbeat(switchService, taskLock,
					taskLockToken, taskLockMinutes);
			heartbeat.renewLocks();
			return heartbeat;
		}
	}
}
