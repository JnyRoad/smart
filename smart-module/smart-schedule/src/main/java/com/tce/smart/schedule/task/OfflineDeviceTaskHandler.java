package com.tce.smart.schedule.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.schedule.config.TaskJob;
import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.schedule.service.platform.DeviceStatusMonitorService;
import com.tce.smart.schedule.service.platform.ISCDeviceTaskService;
import com.tce.smart.schedule.support.IscLogPayloadFormatter;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.TimerTaskEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 离线设备任务处理器
 * 智能处理设备上线后的权限下发
 *
 * @author system
 * @date 2025-06-20
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OfflineDeviceTaskHandler {

    private static final int DEVICE_LOG_SAMPLE_SIZE = 20;

    private final DeviceStatusMonitorService deviceStatusMonitorService;
    private final SmtIscDeviceTaskService smtIscDeviceTaskService;
    private final ISCDeviceTaskService iscDeviceTaskService;
    private final ISwitchService switchService;
    private final TaskJob taskJob;

    @Value("${smart.offline-device.check-window:30}")
    private int checkWindowMinutes = 30; // 检查最近30分钟上线的设备

    /**
     * 处理离线设备上线后的任务重发
     * 每5分钟执行一次
     */
    @Scheduled(fixedDelay = 1000 * 60 * 5, initialDelay = 1000 * 60)
    public void handleOfflineDeviceTasks() {
        if (!taskJob.getIscDeviceOfflineHandler() &&
            !switchService.process(TimerTaskEnum.ISC_DEVICE_OFFLINE_HANDLER)) {
            log.debug("event=isc_offline_device_task_skip reason=disabled");
            return;
        }

        try {
            long startTime = System.currentTimeMillis();
            log.info("event=isc_offline_device_task_run outcome=started check_window_minutes={}", checkWindowMinutes);

            // 1. 获取最近上线的设备
            List<String> recentOnlineDevices = deviceStatusMonitorService.getRecentOnlineDevices(checkWindowMinutes);
            if (CollectionUtil.isEmpty(recentOnlineDevices)) {
                log.info("event=isc_offline_device_task_run outcome=no_recent_online_device elapsed_ms={}",
                    System.currentTimeMillis() - startTime);
                return;
            }

            log.info("event=isc_offline_device_task_devices device_count={} device_sample={}",
                recentOnlineDevices.size(), summarizeDeviceCodes(recentOnlineDevices));

            // 2. 查询这些设备的待处理任务
            List<SmtIscDeviceTask> pendingTasks = smtIscDeviceTaskService.getRecentOnlineDeviceTasks(
                DeviceTaskConstants.CARD, recentOnlineDevices);

            if (CollectionUtil.isEmpty(pendingTasks)) {
                log.info("event=isc_offline_device_task_run outcome=no_pending_task device_count={} elapsed_ms={}",
                    recentOnlineDevices.size(), System.currentTimeMillis() - startTime);
                return;
            }

            log.info("event=isc_offline_device_task_pending task_count={} device_count={}", pendingTasks.size(),
                recentOnlineDevices.size());

            // 3. 智能分类处理任务
            TaskProcessSummary summary = processTasksByType(pendingTasks);

            log.info("event=isc_offline_device_task_run outcome=success task_count={} delete_count={} valid_count={} expired_count={} offline_count={} elapsed_ms={}",
                pendingTasks.size(), summary.deleteCount, summary.validCount, summary.expiredCount, summary.offlineCount,
                System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("event=isc_offline_device_task_run outcome=exception", e);
        }
    }

    /**
     * 按任务类型智能处理
     */
    private TaskProcessSummary processTasksByType(List<SmtIscDeviceTask> tasks) {
        long currentTime = DateUtil.currentSeconds();
        int deleteCount = 0;
        int validCount = 0;
        int expiredCount = 0;
        int offlineCount = 0;

        for (SmtIscDeviceTask task : tasks) {
            try {
                if (isDeleteTask(task)) {
                    // 删除任务：直接标记为待处理，让下次调度执行
                    markTaskForProcessing(task);
                    deleteCount++;
                    log.debug("event=isc_offline_device_task_state task_id={} staff_id={} staff_no={} device_code={} state=ready_for_delete",
                        task.getId(), task.getCardNo(), logTaskBadge(task), task.getDeviceCode());

                } else if (isPermissionStillValid(task, currentTime)) {
                    // 权限仍有效：标记为待处理
                    markTaskForProcessing(task);
                    validCount++;
                    log.debug("event=isc_offline_device_task_state task_id={} staff_id={} staff_no={} device_code={} state=ready_for_dispatch",
                        task.getId(), task.getCardNo(), logTaskBadge(task), task.getDeviceCode());

                } else {
                    // 权限已过期：标记为过期状态
                    markTaskAsExpired(task);
                    expiredCount++;
                    log.debug("event=isc_offline_device_task_state task_id={} staff_id={} staff_no={} device_code={} state=expired",
                        task.getId(), task.getCardNo(), logTaskBadge(task), task.getDeviceCode());
                }

            } catch (Exception e) {
                log.error("event=isc_offline_device_task_state task_id={} staff_id={} staff_no={} device_code={} state=exception",
                    task.getId(), task.getCardNo(), logTaskBadge(task), task.getDeviceCode(), e);
                // 处理异常的任务标记为设备离线状态，等待下次处理
                markTaskAsDeviceOffline(task);
                offlineCount++;
            }
        }

        return new TaskProcessSummary(deleteCount, validCount, expiredCount, offlineCount);
    }

    /**
     * 设备列表仅记录有限样本，避免批量设备上线时写入超长日志。
     */
    private String summarizeDeviceCodes(List<String> deviceCodes) {
        String sample = deviceCodes.stream().limit(DEVICE_LOG_SAMPLE_SIZE).collect(Collectors.joining(","));
        if (deviceCodes.size() <= DEVICE_LOG_SAMPLE_SIZE) {
            return sample;
        }
        return sample + "...[truncated=true,total=" + deviceCodes.size() + "]";
    }

    /**
     * 访客和入厂申请任务的 badge 是证件号，员工任务的 badge 才是工号。
     */
    private String logTaskBadge(SmtIscDeviceTask task) {
        if (DeviceTaskConstants.CARD_VISITOR.equals(task.getServiceType())
            || DeviceTaskConstants.CARD_ADMITTANCE.equals(task.getServiceType())) {
            return IscLogPayloadFormatter.maskCertificate(task.getBadge());
        }
        return task.getBadge();
    }

    /**
     * 离线设备任务处理结果汇总。
     */
    private static final class TaskProcessSummary {
        private final int deleteCount;
        private final int validCount;
        private final int expiredCount;
        private final int offlineCount;

        private TaskProcessSummary(int deleteCount, int validCount, int expiredCount, int offlineCount) {
            this.deleteCount = deleteCount;
            this.validCount = validCount;
            this.expiredCount = expiredCount;
            this.offlineCount = offlineCount;
        }
    }

    /**
     * 判断是否为删除任务
     */
    private boolean isDeleteTask(SmtIscDeviceTask task) {
        return DeviceTaskActionEnum.DEL.getCode().equals(task.getAction()) ||
               DeviceTaskActionEnum.DELAY_DEL.getCode().equals(task.getAction());
    }

    /**
     * 判断权限是否仍然有效
     */
    private boolean isPermissionStillValid(SmtIscDeviceTask task, long currentTime) {
        return task.getOverTime() != null && task.getOverTime() > currentTime;
    }

    /**
     * 标记任务为待处理状态
     */
    private void markTaskForProcessing(SmtIscDeviceTask task) {
        task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
        task.setUpdateTime(LocalDateTime.now());
        task.setRemark("设备上线，重新处理");
        smtIscDeviceTaskService.updateById(task);
    }

    /**
     * 标记任务为过期状态
     */
    private void markTaskAsExpired(SmtIscDeviceTask task) {
        task.setStatus(DeviceTaskStatusEnum.EXPIRED.getCode());
        task.setUpdateTime(LocalDateTime.now());
        task.setRemark("权限已过期，有效期至：" + DateUtil.date(task.getOverTime() * 1000));
        smtIscDeviceTaskService.updateById(task);
    }

    /**
     * 标记任务为设备离线状态
     */
    private void markTaskAsDeviceOffline(SmtIscDeviceTask task) {
        task.setStatus(DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode());
        task.setUpdateTime(LocalDateTime.now());
        task.setRemark("设备处理异常，标记为离线状态");
        smtIscDeviceTaskService.updateById(task);
    }

    /**
     * 清理过期的设备上线记录
     * 每小时执行一次
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60, initialDelay = 1000 * 60 * 10)
    public void cleanExpiredDeviceRecords() {
        try {
            log.debug("开始清理过期设备上线记录");
            deviceStatusMonitorService.cleanExpiredRecords();
        } catch (Exception e) {
            log.error("清理过期设备记录异常", e);
        }
    }
}
