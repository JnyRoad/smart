package com.tce.smart.schedule.task;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.platform.api.feign.admittance.RemoteOaAreaTypeSyncService;
import com.tce.smart.platform.api.feign.securityzone.RemoteSecurityAuthService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.schedule.config.TaskJob;
import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.schedule.service.platform.IDeviceTaskService;
import com.tce.smart.schedule.service.platform.ILeaveApplicationService;
import com.tce.smart.schedule.service.platform.SupplierNotifyService;
import com.tce.smart.tool.enums.TimerTaskEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务服务
 *
 * @author mkwu
 * @date 2019-08-06
 */
@Slf4j
@Component
@EnableScheduling
public class PlatformTimerTask {

	@Autowired
	private IDeviceTaskService deviceTaskService;
	@Autowired
	private RemoteOaAreaTypeSyncService remoteOaAreaTypeSyncService;
	@Autowired
	private SmtDeviceTaskService smtDeviceTaskService;
	@Autowired
	private ILeaveApplicationService leaveApplicationService;
	@Autowired
	private RemoteSecurityAuthService remoteSecurityAuthService;
	@Autowired
	private SupplierNotifyService supplierNotifyService;
	@Autowired
	private ISwitchService switchService;
	@Autowired
	private TaskJob taskJob;

	/**
	 * 设备卡片任务下发
	 * 间隔1分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60)
	public void deviceTaskDownCard() {
		if (taskJob.getDeviceTypeDownCard() && switchService.process(TimerTaskEnum.DEVICE_TYPE_DOWN_CARD)) {
			try {
				deviceTaskService.downCard();
			} catch (Exception e) {
				log.error("卡片下发任务异常", e);
			}
		}
	}

	/**
	 * 设备车辆任务下发
	 * 间隔1分钟执行一次
	 */
	@Scheduled(initialDelay = 1000 * 10, fixedDelay = 1000 * 60)
	public void deviceTaskDown() {
		if (taskJob.getDeviceTypeDownCar() && switchService.process(TimerTaskEnum.DEVICE_TYPE_DOWN_CAR)) {
			deviceTaskService.downCar();
		}
	}

	/**
	 * 设备卡片任务删除
	 * 间隔1分钟执行一次
	 */
	@Scheduled(initialDelay = 1000 * 20, fixedDelay = 1000 * 60)
	public void deviceTaskCard() {
		if (taskJob.getDeviceTypeDelCard() && switchService.process(TimerTaskEnum.DEVICE_TYPE_DEL_CARD)) {
			deviceTaskService.delCard();
		}
	}

	/**
	 * 设备车辆任务删除
	 * 间隔1分钟执行一次
	 */
	@Scheduled(initialDelay = 1000 * 30, fixedDelay = 1000 * 60)
	public void deviceTaskCar() {
		if (taskJob.getDeviceTypeDelCar() && switchService.process(TimerTaskEnum.DEVICE_TYPE_DEL_CAR)) {
			deviceTaskService.delCar();
		}
	}


	/**
	 * 设备任务重发
	 * 每分钟执行一次
	 */
	@Scheduled(fixedRate = 1000 * 60)
	public void repeat() {
		if (taskJob.getDeviceTaskRepeat() && switchService.process(TimerTaskEnum.DEVICE_TASK_REPEAT)) {
			smtDeviceTaskService.repeat();
		}
	}

	/**
	 * 保密区供应商协议过期通知 定时任务
	 * 间隔30分钟执行一次
	 */
	@Scheduled(initialDelay = 1000 * 60, fixedDelay = 1000 * 60 * 30)
	public void supplierNotify() {
		if (taskJob.getSupplierNotify() && switchService.process(TimerTaskEnum.SUPPLIER_NOTIFY)) {
			supplierNotifyService.notifyProcess();
		}
	}


	/**
	 * OA区域类型同步任务 2小时同步一次
	 */
	@Scheduled(fixedDelay = 2000 * 60 * 60)
	public void syncOaAreaType() {
		if (taskJob.getAdmittanceOaAreaType() && switchService.process(TimerTaskEnum.ADMITTANCE_OA_AREA_TYPE)) {
			remoteOaAreaTypeSyncService.syncTask(SecurityConstants.FROM_IN);
		}
	}

	/**
	 * 保密区区域权限自动删除任务每天执行一次
	 */
	@Scheduled(cron = "0 0 00 * * ?")
	public void autoDeleteTask() {
		if (taskJob.getSupplierAutoAuthDelete() && switchService.process(TimerTaskEnum.SUPPLIER_AUTO_AUTH_DELETE)) {
			remoteSecurityAuthService.syncTask(SecurityConstants.FROM_IN);
		}
	}

	/**
	 * 保密区权限下发提示信息推送每20分钟执行一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 20)
	public void securitySendMessage() {
		if (taskJob.getSupplierAuthMsg() && switchService.process(TimerTaskEnum.SUPPLIER_AUTH_MSG)) {
			remoteSecurityAuthService.sendMessage(SecurityConstants.FROM_IN);
		}
	}


	/**
	 * OA审批离职流程同步
	 * 每10分钟同步一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 10)
	public void syncProcessRecord() {
		if (taskJob.getLeaveApplicationProcessType() && switchService.process(TimerTaskEnum.LEAVE_APPLICATION_PROCESS_TYPE)) {
			leaveApplicationService.syncProcessRecord();
		}
	}

}