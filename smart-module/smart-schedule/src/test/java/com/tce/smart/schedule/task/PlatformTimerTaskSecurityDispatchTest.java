package com.tce.smart.schedule.task;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.platform.api.feign.securityzone.RemoteSecurityAuthService;
import com.tce.smart.schedule.config.TaskJob;
import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.tool.enums.TimerTaskEnum;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

/** 保密区异步 worker 调度必须受 Nacos 开关和显式分布式锁双重保护。 */
public class PlatformTimerTaskSecurityDispatchTest {

	@Test
	public void securityAuthDispatchProcess_acquiresLockCallsFeignAndReleasesLock() throws Exception {
		PlatformTimerTask timerTask = new PlatformTimerTask();
		TaskJob taskJob = new TaskJob();
		taskJob.setSecurityAuthDispatchProcess(Boolean.TRUE);
		ISwitchService switchService = Mockito.mock(ISwitchService.class);
		RemoteSecurityAuthService remoteService = Mockito.mock(RemoteSecurityAuthService.class);
		Mockito.when(switchService.acquire(TimerTaskEnum.SECURITY_AUTH_DISPATCH_PROCESS, 1L, TimeUnit.MINUTES))
				.thenReturn("lock-token");
		setField(timerTask, "taskJob", taskJob);
		setField(timerTask, "switchService", switchService);
		setField(timerTask, "remoteSecurityAuthService", remoteService);

		timerTask.securityAuthDispatchProcess();

		Mockito.verify(remoteService).processDispatch(SecurityConstants.FROM_IN);
		Mockito.verify(switchService).release(TimerTaskEnum.SECURITY_AUTH_DISPATCH_PROCESS, "lock-token");
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
