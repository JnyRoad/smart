package com.tce.smart.schedule.service.platform.impl;

import com.tce.smart.schedule.service.platform.DeviceStatusMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 设备状态监控服务实现
 * 使用Redis存储设备上线时间记录
 *
 * @author system
 * @date 2025-06-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceStatusMonitorServiceImpl implements DeviceStatusMonitorService {

    private final StringRedisTemplate redisTemplate;

    private static final String DEVICE_ONLINE_PREFIX = "smart:device:online:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<String> getRecentOnlineDevices(int timeWindowMinutes) {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(timeWindowMinutes);
            String cutoffTimeStr = cutoffTime.format(FORMATTER);

            Set<String> keys = redisTemplate.keys(DEVICE_ONLINE_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return new ArrayList<>();
            }

            return keys.stream()
                    .filter(key -> {
                        String onlineTimeStr = redisTemplate.opsForValue().get(key);
                        if (onlineTimeStr == null) {
                            return false;
                        }
                        // 比较时间字符串（格式相同的情况下可以直接比较）
                        return onlineTimeStr.compareTo(cutoffTimeStr) > 0;
                    })
                    .map(key -> key.substring(DEVICE_ONLINE_PREFIX.length()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取最近上线设备列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void recordDeviceOnline(String deviceCode) {
        try {
            String key = DEVICE_ONLINE_PREFIX + deviceCode;
            String currentTime = LocalDateTime.now().format(FORMATTER);
            // 设置过期时间为24小时，避免数据堆积
            redisTemplate.opsForValue().set(key, currentTime, 24, TimeUnit.HOURS);
            log.debug("记录设备上线：{} at {}", deviceCode, currentTime);
        } catch (Exception e) {
            log.error("记录设备上线时间失败：{}", deviceCode, e);
        }
    }

    @Override
    public boolean isRecentOnline(String deviceCode, int timeWindowMinutes) {
        try {
            String key = DEVICE_ONLINE_PREFIX + deviceCode;
            String onlineTimeStr = redisTemplate.opsForValue().get(key);
            if (onlineTimeStr == null) {
                return false;
            }

            LocalDateTime onlineTime = LocalDateTime.parse(onlineTimeStr, FORMATTER);
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(timeWindowMinutes);

            return onlineTime.isAfter(cutoffTime);
        } catch (Exception e) {
            log.error("检查设备是否最近上线失败：{}", deviceCode, e);
            return false;
        }
    }

    @Override
    public void cleanExpiredRecords() {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusHours(24);
            String expireTimeStr = expireTime.format(FORMATTER);

            Set<String> keys = redisTemplate.keys(DEVICE_ONLINE_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return;
            }

            int cleanedCount = 0;
            for (String key : keys) {
                String onlineTimeStr = redisTemplate.opsForValue().get(key);
                if (onlineTimeStr != null && onlineTimeStr.compareTo(expireTimeStr) <= 0) {
                    redisTemplate.delete(key);
                    cleanedCount++;
                }
            }

            if (cleanedCount > 0) {
                log.info("清理过期设备上线记录：{}条", cleanedCount);
            }
        } catch (Exception e) {
            log.error("清理过期设备上线记录失败", e);
        }
    }
}