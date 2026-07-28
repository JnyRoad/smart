package com.tce.smart.schedule.task;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.feign.RemoteStaffService;
import com.tce.smart.schedule.config.TaskJob;
import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.schedule.service.platform.ISCCardTaskService;
import com.tce.smart.schedule.service.platform.ISCDeviceTaskService;
import com.tce.smart.tool.enums.TimerTaskEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务服务
 *
 * @author wuling
 * @date 2021-08-25
 */
@Slf4j
@Component
@EnableScheduling
public class ISCDeviceTimerTask {

	@Autowired
	private ISCDeviceTaskService deviceTaskService;
	@Autowired
	private ISCCardTaskService cardTaskService;
	@Autowired
	private ISwitchService switchService;
	@Autowired
	private TaskJob taskJob;
	@Autowired
	private RemoteStaffService remoteStaffService;

	/**
	 * ISC设备权限任务下发
	 * 间隔1分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60, initialDelay = 1000 * 10)
	public void deviceTaskDownCard() {
		executeIscJob("device_auth_dispatch", taskJob.getIscDeviceTypeDownCard(),
				TimerTaskEnum.ISC_DEVICE_TYPE_DOWN_CARD, deviceTaskService::downAccess);
	}

	/**
	 * ISC权限配置进度处理
	 * 间隔30秒执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 30, initialDelay = 1000 * 20)
	public void authConfigProcessHandle() {
		executeIscJob("auth_config_progress", taskJob.getIscAuthProcessHandle(),
				TimerTaskEnum.ISC_AUTH_PROCESS_HANDLE, deviceTaskService::authConfigProcessHandle);
	}

	/**
	 * ISC下载权限进度处理
	 * 间隔1分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60, initialDelay = 1000 * 30)
	public void authConfigDownResultHandle() {
		executeIscJob("auth_download_progress", taskJob.getIscAuthResultHandle(),
				TimerTaskEnum.ISC_AUTH_RESULT_HANDLE, deviceTaskService::authConfigDownResultHandle);
	}

	/**
	 * ISC设备权限任务删除
	 * 间隔1分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60, initialDelay = 1000 * 40)
	public void deviceTaskCardDel() {
		executeIscJob("device_auth_delete", taskJob.getIscDeviceTypeDelCard(),
				TimerTaskEnum.ISC_DEVICE_TYPE_DEL_CARD, deviceTaskService::delAccess);
	}

	/**
	 * ISC设备同步任务
	 * 间隔2分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60, initialDelay = 1000 * 20)
	public void deviceSync() {
		executeIscJob("device_sync", taskJob.getIscDeviceSync(), TimerTaskEnum.ISC_DEVICE_SYNC,
				deviceTaskService::syncDevice);
	}

	/**
	 * ISC人员/照片同步失败重试
	 * 间隔10分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 10, initialDelay = 1000 * 60 * 2)
	public void retryIscPersonFaceSync() {
		if (Boolean.TRUE.equals(taskJob.getIscPersonFaceRetry()) && switchService.process(TimerTaskEnum.ISC_PERSON_FACE_RETRY)) {
			long startTime = System.currentTimeMillis();
			log.info("event=isc_scheduler_run job_name=person_face_retry outcome=started");
			try {
				Result<Boolean> result = remoteStaffService.retryFailedIscPersonFaceSync(SecurityConstants.FROM_IN,
						SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
				if (result == null || !result.isSuccess()) {
					log.warn("event=isc_scheduler_run job_name=person_face_retry outcome=remote_failure elapsed_ms={} result_message={}",
							System.currentTimeMillis() - startTime, result == null ? "无响应" : result.getMessage());
					return;
				}
				log.info("event=isc_scheduler_run job_name=person_face_retry outcome=success elapsed_ms={}",
						System.currentTimeMillis() - startTime);
			} catch (Exception e) {
				log.error("event=isc_scheduler_run job_name=person_face_retry outcome=exception elapsed_ms={}",
						System.currentTimeMillis() - startTime, e);
			}
		} else {
			log.debug("event=isc_scheduler_skip job_name=person_face_retry reason=disabled");
		}
	}

	/**
	 * ISC卡片资源同步任务
	 * 间隔1分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60, initialDelay = 1000 * 50)
	public void syncIscCardTasks() {
		executeIscJob("card_task_sync", taskJob.getIscCardTaskSync(), TimerTaskEnum.ISC_CARD_TASK_SYNC,
				cardTaskService::syncCardTasks);
	}

	/**
	 * 统一记录 ISC 定时任务的执行结果，保留既有开关和业务调用顺序。
	 */
	private void executeIscJob(String jobName, Boolean enabled, TimerTaskEnum timerTask, Runnable task) {
		if (!Boolean.TRUE.equals(enabled)) {
			log.debug("event=isc_scheduler_skip job_name={} reason=disabled", jobName);
			return;
		}
		if (!switchService.process(timerTask)) {
			log.debug("event=isc_scheduler_skip job_name={} reason=switch_closed", jobName);
			return;
		}
		long startTime = System.currentTimeMillis();
		log.info("event=isc_scheduler_run job_name={} outcome=started", jobName);
		try {
			task.run();
			log.info("event=isc_scheduler_run job_name={} outcome=success elapsed_ms={}", jobName,
					System.currentTimeMillis() - startTime);
		} catch (Exception e) {
			log.error("event=isc_scheduler_run job_name={} outcome=exception elapsed_ms={}", jobName,
					System.currentTimeMillis() - startTime, e);
		}
	}

	/**
	 * ISC设备测温获取
	 * 每二十分钟执行一次
	 * 任务里的延迟时间需要随着这里的执行时间同步修改
	 */
	/**
	@Scheduled(fixedRate = 1000 * 60 * 20)
	public void temperatureGet() {
		if (taskJob.getIscTemperatureGet() && switchService.process(TimerTaskEnum.ISC_TEMPERATURE_GET)) {
			log.info("ISC设备测温获取任务开始执行");
			deviceTaskService.getTemperature();
		}
	}
	*/
}
