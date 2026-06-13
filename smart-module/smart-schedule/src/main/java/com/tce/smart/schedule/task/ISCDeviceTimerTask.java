package com.tce.smart.schedule.task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
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
		if (taskJob.getIscDeviceTypeDownCard() && switchService.process(TimerTaskEnum.ISC_DEVICE_TYPE_DOWN_CARD)) {
			deviceTaskService.downAccess();
		}
	}

	/**
	 * ISC权限配置进度处理
	 * 间隔30秒执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 30, initialDelay = 1000 * 20)
	public void authConfigProcessHandle() {
		if (taskJob.getIscAuthProcessHandle() && switchService.process(TimerTaskEnum.ISC_AUTH_PROCESS_HANDLE)) {
			deviceTaskService.authConfigProcessHandle();
		}
	}

	/**
	 * ISC下载权限进度处理
	 * 间隔1分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60, initialDelay = 1000 * 30)
	public void authConfigDownResultHandle() {
		if (taskJob.getIscAuthResultHandle() && switchService.process(TimerTaskEnum.ISC_AUTH_RESULT_HANDLE)) {
			DateTime start = DateUtil.date();
			log.info("ISC下载权限进度处理开始：{}", start);
			try {
				deviceTaskService.authConfigDownResultHandle();
			} catch (Exception e) {
				log.info("ISC下载权限进度处理异常", e);
			}
			log.info("ISC下载权限进度处理开始：{}, 结束：{}", start, DateUtil.date());
		}
	}

	/**
	 * ISC设备权限任务删除
	 * 间隔1分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60, initialDelay = 1000 * 40)
	public void deviceTaskCardDel() {
		if (taskJob.getIscDeviceTypeDelCard() && switchService.process(TimerTaskEnum.ISC_DEVICE_TYPE_DEL_CARD)) {
			deviceTaskService.delAccess();
		}
	}

	/**
	 * ISC设备同步任务
	 * 间隔2分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60, initialDelay = 1000 * 20)
	public void deviceSync() {
		if (taskJob.getIscDeviceSync() && switchService.process(TimerTaskEnum.ISC_DEVICE_SYNC)) {
			deviceTaskService.syncDevice();
		}
	}

	/**
	 * ISC人员/照片同步失败重试
	 * 间隔10分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 10, initialDelay = 1000 * 60 * 2)
	public void retryIscPersonFaceSync() {
		if (Boolean.TRUE.equals(taskJob.getIscPersonFaceRetry()) && switchService.process(TimerTaskEnum.ISC_PERSON_FACE_RETRY)) {
			try {
				Result<Boolean> result = remoteStaffService.retryFailedIscPersonFaceSync(SecurityConstants.FROM_IN);
				if (result == null || !result.isSuccess()) {
					log.warn("ISC人员照片同步失败重试任务调用失败：{}", result == null ? "无响应" : result.getMessage());
				}
			} catch (Exception e) {
				log.error("ISC人员照片同步失败重试任务异常", e);
			}
		}
	}

	/**
	 * ISC卡片资源同步任务
	 * 间隔1分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60, initialDelay = 1000 * 50)
	public void syncIscCardTasks() {
		if (Boolean.TRUE.equals(taskJob.getIscCardTaskSync()) && switchService.process(TimerTaskEnum.ISC_CARD_TASK_SYNC)) {
			cardTaskService.syncCardTasks();
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
