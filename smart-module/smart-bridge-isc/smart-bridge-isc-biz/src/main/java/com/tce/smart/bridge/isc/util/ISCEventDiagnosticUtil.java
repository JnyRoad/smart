package com.tce.smart.bridge.isc.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ISC 事件诊断工具类
 * 用于分析和统计人员编号为空的问题
 *
 * @author system
 * @date 2025-01-11
 */
@Slf4j
public class ISCEventDiagnosticUtil {

    // 统计计数器
    private static final AtomicLong TOTAL_EVENTS = new AtomicLong(0);
    private static final AtomicLong EMPTY_PERSON_NO_EVENTS = new AtomicLong(0);
    private static final AtomicLong PERSON_NOT_FOUND_EVENTS = new AtomicLong(0);
    private static final AtomicLong SUCCESS_EVENTS = new AtomicLong(0);

    // 设备统计 - 记录每个设备的问题次数
    private static final Map<String, AtomicLong> DEVICE_EMPTY_PERSON_COUNT = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> DEVICE_TOTAL_COUNT = new ConcurrentHashMap<>();

    // 事件类型统计
    private static final Map<Integer, AtomicLong> EVENT_TYPE_EMPTY_COUNT = new ConcurrentHashMap<>();

    /**
     * 记录事件处理开始
     */
    public static void recordEventStart() {
        TOTAL_EVENTS.incrementAndGet();
    }

    /**
     * 记录人员编号为空的事件
     *
     * @param eventType 事件类型
     * @param deviceCode 设备编码
     * @param eventData 事件数据
     */
    public static void recordEmptyPersonNo(Integer eventType, String deviceCode, JSONObject eventData) {
        EMPTY_PERSON_NO_EVENTS.incrementAndGet();

        // 设备统计
        if (StrUtil.isNotBlank(deviceCode)) {
            DEVICE_EMPTY_PERSON_COUNT.computeIfAbsent(deviceCode, k -> new AtomicLong(0)).incrementAndGet();
            DEVICE_TOTAL_COUNT.computeIfAbsent(deviceCode, k -> new AtomicLong(0)).incrementAndGet();
        }

        // 事件类型统计
        if (eventType != null) {
            EVENT_TYPE_EMPTY_COUNT.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
        }

        // 详细分析事件数据
        analyzeEventData(eventType, deviceCode, eventData);
    }

    /**
     * 记录人员未找到的事件
     */
    public static void recordPersonNotFound(String deviceCode) {
        PERSON_NOT_FOUND_EVENTS.incrementAndGet();

        if (StrUtil.isNotBlank(deviceCode)) {
            DEVICE_TOTAL_COUNT.computeIfAbsent(deviceCode, k -> new AtomicLong(0)).incrementAndGet();
        }

        log.warn("ISC 人员查询失败，已按设备维度记录");
    }

    /**
     * 记录成功处理的事件
     */
    public static void recordSuccess(String deviceCode) {
        SUCCESS_EVENTS.incrementAndGet();

        if (StrUtil.isNotBlank(deviceCode)) {
            DEVICE_TOTAL_COUNT.computeIfAbsent(deviceCode, k -> new AtomicLong(0)).incrementAndGet();
        }
    }

    /**
     * 分析事件数据，寻找可能的原因
     */
    private static void analyzeEventData(Integer eventType, String deviceCode, JSONObject eventData) {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("eventType", eventType);
        analysis.put("deviceCode", deviceCode);
        analysis.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 检查可能的人员标识字段
        String[] personFields = {"ExtEventPersonNo", "cardNo", "jobNo", "personName", "personId"};
        int presentPersonFieldCount = 0;

        for (String field : personFields) {
            String value = eventData.getStr(field);
            if (StrUtil.isNotBlank(value)) {
                presentPersonFieldCount++;
            }
        }

        analysis.put("presentPersonFieldCount", presentPersonFieldCount);

        // 检查其他关键字段
        analysis.put("inOutType", eventData.getStr("ExtEventInOut"));
        analysis.put("hasPictureUrl", StrUtil.isNotBlank(eventData.getStr("ExtEventPictureURL")));
        analysis.put("hasServerIndexCode", StrUtil.isNotBlank(eventData.getStr("svrIndexCode")));

        log.warn("人员编号为空事件分析: {}", JSONUtil.toJsonStr(analysis));
    }

    /**
     * 获取统计报告
     */
    public static String getStatisticsReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n=== ISC事件处理统计报告 ===\n");
        report.append(String.format("总事件数: %d\n", TOTAL_EVENTS.get()));
        report.append(String.format("成功处理: %d (%.2f%%)\n",
                SUCCESS_EVENTS.get(),
                TOTAL_EVENTS.get() > 0 ? (SUCCESS_EVENTS.get() * 100.0 / TOTAL_EVENTS.get()) : 0));
        report.append(String.format("人员编号为空: %d (%.2f%%)\n",
                EMPTY_PERSON_NO_EVENTS.get(),
                TOTAL_EVENTS.get() > 0 ? (EMPTY_PERSON_NO_EVENTS.get() * 100.0 / TOTAL_EVENTS.get()) : 0));
        report.append(String.format("人员未找到: %d (%.2f%%)\n",
                PERSON_NOT_FOUND_EVENTS.get(),
                TOTAL_EVENTS.get() > 0 ? (PERSON_NOT_FOUND_EVENTS.get() * 100.0 / TOTAL_EVENTS.get()) : 0));

        // 设备统计
        report.append("\n--- 设备问题统计 ---\n");
        DEVICE_EMPTY_PERSON_COUNT.forEach((device, count) -> {
            long total = DEVICE_TOTAL_COUNT.getOrDefault(device, new AtomicLong(0)).get();
            double rate = total > 0 ? (count.get() * 100.0 / total) : 0;
            report.append(String.format("设备 %s: 人员编号为空 %d/%d (%.2f%%)\n",
                    device, count.get(), total, rate));
        });

        // 事件类型统计
        report.append("\n--- 事件类型问题统计 ---\n");
        EVENT_TYPE_EMPTY_COUNT.forEach((eventType, count) -> {
            report.append(String.format("事件类型 %d: 人员编号为空 %d 次\n", eventType, count.get()));
        });

        report.append("========================\n");
        return report.toString();
    }

    /**
     * 重置统计数据
     */
    public static void resetStatistics() {
        TOTAL_EVENTS.set(0);
        EMPTY_PERSON_NO_EVENTS.set(0);
        PERSON_NOT_FOUND_EVENTS.set(0);
        SUCCESS_EVENTS.set(0);
        DEVICE_EMPTY_PERSON_COUNT.clear();
        DEVICE_TOTAL_COUNT.clear();
        EVENT_TYPE_EMPTY_COUNT.clear();

        log.info("ISC事件统计数据已重置");
    }

    /**
     * 检查是否需要告警
     * 当人员编号为空的比例超过阈值时返回true
     */
    public static boolean shouldAlert() {
        long total = TOTAL_EVENTS.get();
        long empty = EMPTY_PERSON_NO_EVENTS.get();

        // 当总事件数超过100且人员编号为空比例超过20%时告警
        if (total > 100 && empty * 100.0 / total > 20) {
            return true;
        }

        // 当某个设备的问题比例超过50%时告警
        for (Map.Entry<String, AtomicLong> entry : DEVICE_EMPTY_PERSON_COUNT.entrySet()) {
            String device = entry.getKey();
            long deviceEmpty = entry.getValue().get();
            long deviceTotal = DEVICE_TOTAL_COUNT.getOrDefault(device, new AtomicLong(0)).get();

            if (deviceTotal > 10 && deviceEmpty * 100.0 / deviceTotal > 50) {
                log.warn("设备 {} 人员编号为空比例过高: {}/{} ({}%)",
                        device, deviceEmpty, deviceTotal, deviceEmpty * 100.0 / deviceTotal);
                return true;
            }
        }

        return false;
    }
}
