package com.tce.smart.schedule.task;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.platform.api.feign.admittance.RemoteAdmittanceTaskService;
import com.tce.smart.schedule.config.TaskJob;
import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.tool.enums.TimerTaskEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

public class EHRViewTimerTaskTest {

	@Test
	public void admittanceUpdateOaLockTtlLimitsUnlockFailureDelay() throws Exception {
		Field field = EHRViewTimerTask.class.getDeclaredField("ADMITTANCE_UPDATE_OA_LOCK_MINUTES");
		field.setAccessible(true);

		long lockMinutes = field.getLong(null);

		Assert.assertEquals(5L, lockMinutes);
	}

	@Test
	public void admittanceUpdateOaTaskReleasesLockAfterRemoteCall() throws Exception {
		TaskJob taskJob = Mockito.mock(TaskJob.class);
		ISwitchService switchService = Mockito.mock(ISwitchService.class);
		RemoteAdmittanceTaskService remoteAdmittanceTaskService = Mockito.mock(RemoteAdmittanceTaskService.class);
		EHRViewTimerTask task = new EHRViewTimerTask();
		setField(task, "taskJob", taskJob);
		setField(task, "iSwitchService", switchService);
		setField(task, "remoteAdmittanceTaskService", remoteAdmittanceTaskService);
		Mockito.when(taskJob.getAdmittanceUpdateOa()).thenReturn(Boolean.TRUE);
		Mockito.when(switchService.acquire(TimerTaskEnum.ADMITTANCE_UPDATE_OA, 5L, TimeUnit.MINUTES)).thenReturn("owner-token");

		task.admittanceUpdateOaTask();

		Mockito.verify(remoteAdmittanceTaskService).updateOaStatusTask(SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		Mockito.verify(switchService).release(TimerTaskEnum.ADMITTANCE_UPDATE_OA, "owner-token");
	}

	@Test
	public void admittanceUpdateOaTaskReleasesLockWhenRemoteCallFails() throws Exception {
		TaskJob taskJob = Mockito.mock(TaskJob.class);
		ISwitchService switchService = Mockito.mock(ISwitchService.class);
		RemoteAdmittanceTaskService remoteAdmittanceTaskService = Mockito.mock(RemoteAdmittanceTaskService.class);
		EHRViewTimerTask task = new EHRViewTimerTask();
		setField(task, "taskJob", taskJob);
		setField(task, "iSwitchService", switchService);
		setField(task, "remoteAdmittanceTaskService", remoteAdmittanceTaskService);
		Mockito.when(taskJob.getAdmittanceUpdateOa()).thenReturn(Boolean.TRUE);
		Mockito.when(switchService.acquire(TimerTaskEnum.ADMITTANCE_UPDATE_OA, 5L, TimeUnit.MINUTES)).thenReturn("owner-token");
		Mockito.doThrow(new RuntimeException("remote failed"))
				.when(remoteAdmittanceTaskService).updateOaStatusTask(SecurityConstants.FROM_IN,
						SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);

		try {
			task.admittanceUpdateOaTask();
			Assert.fail("remote failure should propagate");
		} catch (RuntimeException expected) {
			Assert.assertEquals("remote failed", expected.getMessage());
		}

		Mockito.verify(switchService).release(TimerTaskEnum.ADMITTANCE_UPDATE_OA, "owner-token");
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
