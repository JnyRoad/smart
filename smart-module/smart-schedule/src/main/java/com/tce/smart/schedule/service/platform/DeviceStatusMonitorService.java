package com.tce.smart.schedule.service.platform;

import java.util.List;

/**
 * 设备状态监控服务
 * 用于跟踪设备上线状态和管理离线设备任务
 *
 * @author system
 * @date 2025-06-20
 */
public interface DeviceStatusMonitorService {

    /**
     * 获取最近上线的设备列表
     * @param timeWindowMinutes 时间窗口（分钟）
     * @return 最近上线的设备编码列表
     */
    List<String> getRecentOnlineDevices(int timeWindowMinutes);

    /**
     * 记录设备上线时间
     * @param deviceCode 设备编码
     */
    void recordDeviceOnline(String deviceCode);

    /**
     * 检查设备是否最近上线
     * @param deviceCode 设备编码
     * @param timeWindowMinutes 时间窗口（分钟）
     * @return 是否最近上线
     */
    boolean isRecentOnline(String deviceCode, int timeWindowMinutes);

    /**
     * 清理过期的设备上线记录
     */
    void cleanExpiredRecords();
}