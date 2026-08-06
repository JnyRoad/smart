package com.tce.smart.schedule.task;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.platform.api.feign.RemoteOaCallbackLogService;
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

import java.util.concurrent.TimeUnit;

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

	/**
	 * 保密门禁OA对账任务锁TTL：5分钟。
	 * 覆盖单轮最长耗时（两批×200单×OA查询超时5s 的量级裕度），避免任务未跑完锁已过期导致重入。
	 */
	private static final long SECURITY_AUTH_UPDATE_OA_LOCK_MINUTES = 5L;

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
	private RemoteOaCallbackLogService remoteOaCallbackLogService;
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

	/**
	 * 保密门禁申请OA审批状态对账 每2分钟执行一次（spec §3.1.6，Nacos 开关默认关）
	 * 使用 acquire/release 而非 process：单轮任务耗时不确定（受 OA 接口响应影响），
	 * 需要显式持锁避免下一轮调度在当前批次未结束时并发触发。
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 2)
	public void securityAuthUpdateOaTask() {
		if (taskJob.getSecurityAuthUpdateOa() == null || !taskJob.getSecurityAuthUpdateOa()) {
			return;
		}
		String lockToken = switchService.acquire(TimerTaskEnum.SECURITY_AUTH_UPDATE_OA,
				SECURITY_AUTH_UPDATE_OA_LOCK_MINUTES, TimeUnit.MINUTES);
		if (lockToken == null) {
			return;
		}
		try {
			remoteSecurityAuthService.updateOaStatusTask(SecurityConstants.FROM_IN);
		} finally {
			switchService.release(TimerTaskEnum.SECURITY_AUTH_UPDATE_OA, lockToken);
		}
	}

	/**
	 * OA回调日志过期清理 每天03:30执行一次（90 天整行删除，payload 含 PII，spec 2026-07-05 §3.2）
	 */
	@Scheduled(cron = "0 30 3 * * ?")
	public void oaCallbackLogClean() {
		if (taskJob.getOaCallbackLogClean() != null && taskJob.getOaCallbackLogClean()
				&& switchService.process(TimerTaskEnum.OA_CALLBACK_LOG_CLEAN)) {
			try {
				remoteOaCallbackLogService.cleanTask(SecurityConstants.FROM_IN);
			} catch (Exception e) {
				log.error("OA回调日志过期清理任务异常", e);
			}
		}
	}

}