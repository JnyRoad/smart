package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * 直连设备可信结果的本地收敛边界，调用方须在外部请求结束后通过独立 Bean 调用。
 * 任务 CAS、下发记录和业务派生任务共同提交；不命中时不产生后续副作用。
 */
@Service
public class DirectTaskCompletionService {
    private AuthOperationDirectTakeoverService directTakeover;
    @org.springframework.beans.factory.annotation.Autowired
    public void setDirectTakeover(AuthOperationDirectTakeoverService service) { this.directTakeover=service; }
    private final SmtDeviceTaskService tasks;
    private final SmtTaskDownRecordService records;

    public DirectTaskCompletionService(SmtDeviceTaskService tasks, SmtTaskDownRecordService records) {
        this.tasks = tasks;
        this.records = records;
    }

    /**
     * 只接收适配器已验证的实际设备成功证据；受理结果必须调用 recordResult。
     * 回调只允许执行本地业务写入，失败须抛异常，以便 REQUIRED 事务完整回滚。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean completeSuccess(SmtDeviceTask task, Integer code, String remark,
            Consumer<SmtDeviceTask> afterRecord) {
        if (!canApply(task)) {
            return false;
        }
        com.tce.smart.platform.core.entity.SmtAuthTransportPhase context=AuthOperationTransportRecordContext.current("DIRECT",String.valueOf(task.getId()));
        boolean trusted=org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()
                && AuthOperationDirectTakeoverService.matchesPhase(context,task);
        if(!trusted) {
            if(directTakeover==null)throw new IllegalStateException("DIRECT 成功门禁未装配");
            com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.Decision decision=directTakeover.admitLegacyDirect(task.getId(),
                    com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.LegacyIdentity.of(task));
            if(decision==null || !decision.legacyAllowed())return false;
        }
        // 复制快照，保留调用方的原状态，回滚后不会因内存提前成功而失去重试机会。
        SmtDeviceTask completed = new SmtDeviceTask();
        BeanUtils.copyProperties(task, completed);
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<SmtDeviceTask> update = currentCommand(completed)
                .set(SmtDeviceTask::getStatus, DeviceTaskStatusEnum.SUCCESS.getCode())
                .set(SmtDeviceTask::getCode, code)
                .set(SmtDeviceTask::getRemark, remark)
                .set(SmtDeviceTask::getUpdateTime, now);
        if (completed.getConsume() != null) {
            update.set(SmtDeviceTask::getConsume, completed.getConsume());
        }
        if (!tasks.update(update)) {
            return false;
        }
        completed.setStatus(DeviceTaskStatusEnum.SUCCESS.getCode());
        completed.setCode(code);
        completed.setRemark(remark);
        completed.setUpdateTime(now);
        records.handleTaskDownRecord(completed);
        if (afterRecord != null) {
            afterRecord.accept(completed);
        }
        return true;
    }

    /** 保存受理、失败或待重试结果，不能通过该入口写成功或覆盖终态。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean recordResult(SmtDeviceTask task, Integer status, Integer code, String remark, Long consume) {
        if (!canApply(task)) {
            return false;
        }
        if (status != null && !DeviceTaskStatusEnum.FAIL.getCode().equals(status)) {
            throw new IllegalArgumentException("直连非成功结果只允许失败或保留当前状态");
        }
        LambdaUpdateWrapper<SmtDeviceTask> update = currentCommand(task)
                .set(SmtDeviceTask::getCode, code)
                .set(SmtDeviceTask::getRemark, remark)
                .set(SmtDeviceTask::getUpdateTime, LocalDateTime.now());
        if (status != null) {
            update.set(SmtDeviceTask::getStatus, status);
        }
        if (consume != null) {
            update.set(SmtDeviceTask::getConsume, consume);
        }
        return tasks.update(update);
    }

    /** 发送前只累加次数，禁止把调度器旧快照的状态和命令整体写回。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean recordDispatchAttempt(SmtDeviceTask task) {
        if (!canApply(task)) {
            return false;
        }
        return tasks.update(currentCommand(task).setSql("TIMES = NVL(TIMES, 0) + 1"));
    }

    /** 直连受理不一定写 DOING，兼容空初始化值和现有可重试状态。 */
    private boolean canApply(SmtDeviceTask task) {
        if (task == null || task.getId() == null || task.getAction() == null
                || task.getSerialNo() == null || task.getSerialNo().trim().isEmpty()) {
            return false;
        }
        Integer status = task.getStatus();
        return status == null || DeviceTaskStatusEnum.INIT.getCode().equals(status)
                || DeviceTaskStatusEnum.DOING.getCode().equals(status)
                || DeviceTaskStatusEnum.FAIL.getCode().equals(status)
                || DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode().equals(status);
    }

    /** 同时冻结主键、流水号、操作和旧状态，迟到回执不得覆盖更新后的命令。 */
    private LambdaUpdateWrapper<SmtDeviceTask> currentCommand(SmtDeviceTask task) {
        LambdaUpdateWrapper<SmtDeviceTask> update = new LambdaUpdateWrapper<SmtDeviceTask>()
                .eq(SmtDeviceTask::getId, task.getId())
                .eq(SmtDeviceTask::getSerialNo, task.getSerialNo())
                .eq(SmtDeviceTask::getAction, task.getAction());
        // 门禁读取之后仍按冻结身份 CAS，防止同流水号被原地改写后落入另一资源。
        if(task.getDeviceCode()==null)update.isNull(SmtDeviceTask::getDeviceCode);else update.eq(SmtDeviceTask::getDeviceCode,task.getDeviceCode());
        if(task.getCardNo()==null)update.isNull(SmtDeviceTask::getCardNo);else update.eq(SmtDeviceTask::getCardNo,task.getCardNo());
        if(task.getDeviceType()==null)update.isNull(SmtDeviceTask::getDeviceType);else update.eq(SmtDeviceTask::getDeviceType,task.getDeviceType());
        if(task.getServiceType()==null)update.isNull(SmtDeviceTask::getServiceType);else update.eq(SmtDeviceTask::getServiceType,task.getServiceType());
        if(task.getCardType()==null)update.isNull(SmtDeviceTask::getCardType);else update.eq(SmtDeviceTask::getCardType,task.getCardType());
        if(task.getGeneral()==null)update.isNull(SmtDeviceTask::getGeneral);else update.eq(SmtDeviceTask::getGeneral,task.getGeneral());
        if(task.getImageId()==null)update.isNull(SmtDeviceTask::getImageId);else update.eq(SmtDeviceTask::getImageId,task.getImageId());
        if(task.getStartTime()==null)update.isNull(SmtDeviceTask::getStartTime);else update.eq(SmtDeviceTask::getStartTime,task.getStartTime());
        if(task.getOverTime()==null)update.isNull(SmtDeviceTask::getOverTime);else update.eq(SmtDeviceTask::getOverTime,task.getOverTime());
        if (task.getStatus() == null) {
            update.isNull(SmtDeviceTask::getStatus);
        } else {
            update.eq(SmtDeviceTask::getStatus, task.getStatus());
        }
        return update;
    }
}
