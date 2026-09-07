package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.enums.ISCDeviceTaskEnum;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * ISC 设备可信成功结果的本地原子收敛服务。
 *
 * <p>该服务必须通过 Spring 代理调用；独立 Bean 提供事务边界，使任务状态更新和下发记录维护
 * 参与同一事务，防止状态先提交而记录维护失败后丢失可恢复依据。</p>
 */
@Service
public class IscTaskCompletionService {

	private final SmtIscDeviceTaskService smtIscDeviceTaskService;

	private final SmtIscDownRecordService smtIscDownRecordService;

	/**
	 * 创建 ISC 成功收敛服务。
	 *
	 * @param smtIscDeviceTaskService ISC 设备任务服务
	 * @param smtIscDownRecordService ISC 下发记录服务
	 */
	public IscTaskCompletionService(SmtIscDeviceTaskService smtIscDeviceTaskService,
			SmtIscDownRecordService smtIscDownRecordService) {
		this.smtIscDeviceTaskService = smtIscDeviceTaskService;
		this.smtIscDownRecordService = smtIscDownRecordService;
	}

	/**
	 * 在 Spring 事务边界内完成已取得设备成功证据的任务。
	 *
	 * <p>处理步骤：先保存原任务状态和 ISC 批次号；再按任务主键、原状态及原批次号条件更新；
	 * 条件更新命中后维护下发记录，记录维护异常直接向调用方抛出以触发事务回滚。</p>
	 *
	 * @param task 已取得可信 ISC 设备成功证据的持久任务
	 * @param remark 成功备注，空值时使用默认成功描述
	 * @return 条件更新命中且下发记录维护完成返回 true，条件未命中返回 false
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean completeSuccess(SmtIscDeviceTask task, String remark) {
		if (task == null || task.getId() == null) {
			throw new IllegalArgumentException("ISC成功收敛任务及任务ID不能为空");
		}

		// 第一步：在修改任务对象前保留本次回执对应的持久状态和外部批次号。
		Integer originalStatus = task.getStatus();
		String originalIscTaskId = task.getIscTaskId();
		if (!DeviceTaskStatusEnum.DOING.getCode().equals(originalStatus)) {
			// 成功回执只允许收敛处理中任务，终态或未受理任务不能被重复改写。
			return false;
		}
		String successRemark = remark == null || remark.trim().isEmpty()
				? ISCDeviceTaskEnum.DEVICE_OK.getDesc() : remark;
		LocalDateTime updateTime = LocalDateTime.now();

		// 第二步：只允许仍属于本次外部尝试的原任务转为成功，避免旧回执覆盖新尝试。
		LambdaUpdateWrapper<SmtIscDeviceTask> updateWrapper = new LambdaUpdateWrapper<SmtIscDeviceTask>()
				.set(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.SUCCESS.getCode())
				.set(SmtIscDeviceTask::getCode, ISCDeviceTaskEnum.DEVICE_OK.getCode())
				.set(SmtIscDeviceTask::getRemark, successRemark)
				.set(SmtIscDeviceTask::getUpdateTime, updateTime)
				.eq(SmtIscDeviceTask::getId, task.getId());
		updateWrapper.eq(SmtIscDeviceTask::getStatus, originalStatus);
		if (originalIscTaskId == null) {
			updateWrapper.isNull(SmtIscDeviceTask::getIscTaskId);
		} else {
			updateWrapper.eq(SmtIscDeviceTask::getIscTaskId, originalIscTaskId);
		}

		boolean updated = smtIscDeviceTaskService.update(updateWrapper);
		if (!updated) {
			// 条件未命中表示任务已被其他尝试处理，本次回执不能重复维护下发记录。
			return false;
		}

		// 第三步：同步回传对象并维护本地下发记录；异常必须继续抛出并回滚本事务。
		task.setStatus(DeviceTaskStatusEnum.SUCCESS.getCode());
		task.setCode(ISCDeviceTaskEnum.DEVICE_OK.getCode());
		task.setRemark(successRemark);
		task.setUpdateTime(updateTime);
		smtIscDownRecordService.handleTaskDownRecord(task);
		return true;
	}
}
