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
            return;
        }

        try {
            long startTime = System.currentTimeMillis();
            log.info("开始处理离线设备上线任务重发");

            // 1. 获取最近上线的设备
            List<String> recentOnlineDevices = deviceStatusMonitorService.getRecentOnlineDevices(checkWindowMinutes);
            if (CollectionUtil.isEmpty(recentOnlineDevices)) {
                log.debug("没有发现最近上线的设备");
                return;
            }

            log.info("发现最近上线设备：{}台，设备列表：{}", recentOnlineDevices.size(), recentOnlineDevices);

            // 2. 查询这些设备的待处理任务
            List<SmtIscDeviceTask> pendingTasks = smtIscDeviceTaskService.getRecentOnlineDeviceTasks(
                DeviceTaskConstants.CARD, recentOnlineDevices);

            if (CollectionUtil.isEmpty(pendingTasks)) {
                log.info("最近上线设备无待处理任务");
                return;
            }

            log.info("发现待处理任务：{}个", pendingTasks.size());

            // 3. 智能分类处理任务
            processTasksByType(pendingTasks);

            log.info("离线设备任务处理完成，耗时：{}ms", System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("处理离线设备任务异常", e);
        }
    }

    /**
     * 按任务类型智能处理
     */
    private void processTasksByType(List<SmtIscDeviceTask> tasks) {
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
                    log.debug("删除任务标记为待处理：{}", task.getId());

                } else if (isPermissionStillValid(task, currentTime)) {
                    // 权限仍有效：标记为待处理
                    markTaskForProcessing(task);
                    validCount++;
                    log.debug("有效权限任务标记为待处理：{}", task.getId());

                } else {
                    // 权限已过期：标记为过期状态
                    markTaskAsExpired(task);
                    expiredCount++;
                    log.debug("过期任务标记为过期：{}", task.getId());
                }

            } catch (Exception e) {
                log.error("处理任务异常，任务ID：{}", task.getId(), e);
                // 处理异常的任务标记为设备离线状态，等待下次处理
                markTaskAsDeviceOffline(task);
                offlineCount++;
            }
        }

        log.info("任务分类处理完成 - 删除任务：{}，有效任务：{}，过期任务：{}，离线任务：{}",
                deleteCount, validCount, expiredCount, offlineCount);
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