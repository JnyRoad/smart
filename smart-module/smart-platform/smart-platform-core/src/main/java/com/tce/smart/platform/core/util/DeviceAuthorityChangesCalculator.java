package com.tce.smart.platform.core.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备权限变更计算工具类
 * 用于高效计算设备权限变更的差异，支持批量处理
 *
 * @author 李世勋 (lisx)
 * @date 2025-06-25
 */
@Slf4j
public class DeviceAuthorityChangesCalculator {

    /**
     * 设备变更结果类
     */
    @Data
    public static class DeviceChanges {
        /** 需要删除权限的设备列表 */
        private final List<String> devicesToRemove;
        /** 需要添加权限的设备列表 */
        private final List<String> devicesToAdd;
        /** 保持不变的设备列表 */
        private final List<String> unchangedDevices;

        public DeviceChanges(List<String> devicesToRemove, List<String> devicesToAdd, List<String> unchangedDevices) {
            this.devicesToRemove = devicesToRemove != null ? devicesToRemove : new ArrayList<>();
            this.devicesToAdd = devicesToAdd != null ? devicesToAdd : new ArrayList<>();
            this.unchangedDevices = unchangedDevices != null ? unchangedDevices : new ArrayList<>();
        }

        /**
         * 是否有设备变更
         */
        public boolean hasChanges() {
            return !devicesToRemove.isEmpty() || !devicesToAdd.isEmpty();
        }

        /**
         * 获取变更统计信息
         */
        public String getChangesSummary() {
            return String.format("删除设备:%d个, 新增设备:%d个, 不变设备:%d个",
                devicesToRemove.size(), devicesToAdd.size(), unchangedDevices.size());
        }
    }

    /**
     * 计算设备权限变更差异
     *
     * @param oldDeviceIds 原有设备ID列表
     * @param newDeviceIds 新的设备ID数组
     * @return 设备变更结果
     */
    public static DeviceChanges calculateChanges(List<String> oldDeviceIds, String[] newDeviceIds) {
        if (oldDeviceIds == null) {
            oldDeviceIds = new ArrayList<>();
        }
        if (newDeviceIds == null) {
            newDeviceIds = new String[0];
        }

        // 使用HashSet提高查找效率
        Set<String> oldDeviceSet = new HashSet<>(oldDeviceIds);
        Set<String> newDeviceSet = Arrays.stream(newDeviceIds)
            .filter(id -> id != null && !id.trim().isEmpty())
            .collect(Collectors.toSet());

        // 计算需要删除的设备（在旧列表中但不在新列表中）
        Set<String> toRemove = new HashSet<>(oldDeviceSet);
        toRemove.removeAll(newDeviceSet);

        // 计算需要添加的设备（在新列表中但不在旧列表中）
        Set<String> toAdd = new HashSet<>(newDeviceSet);
        toAdd.removeAll(oldDeviceSet);

        // 计算保持不变的设备（既在旧列表又在新列表中）
        Set<String> unchanged = new HashSet<>(oldDeviceSet);
        unchanged.retainAll(newDeviceSet);

        DeviceChanges changes = new DeviceChanges(
            new ArrayList<>(toRemove),
            new ArrayList<>(toAdd),
            new ArrayList<>(unchanged)
        );

        log.info("设备权限变更计算完成: {}", changes.getChangesSummary());
        return changes;
    }

    /**
     * 验证设备变更的合理性
     *
     * @param changes 设备变更结果
     * @param maxChangesAllowed 允许的最大变更数量
     * @return 是否合理
     */
    public static boolean validateChanges(DeviceChanges changes, int maxChangesAllowed) {
        int totalChanges = changes.getDevicesToRemove().size() + changes.getDevicesToAdd().size();

        if (totalChanges > maxChangesAllowed) {
            log.warn("设备变更数量过大: 当前变更{}个, 最大允许{}个", totalChanges, maxChangesAllowed);
            return false;
        }

        return true;
    }

    /**
     * 估算任务数量
     *
     * @param changes 设备变更结果
     * @param userCount 用户数量
     * @return 预估的任务数量
     */
    public static int estimateTaskCount(DeviceChanges changes, int userCount) {
        int deviceChanges = changes.getDevicesToRemove().size() + changes.getDevicesToAdd().size();
        return deviceChanges * userCount;
    }
}