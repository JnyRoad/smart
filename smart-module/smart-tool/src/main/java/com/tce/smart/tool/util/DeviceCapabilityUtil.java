package com.tce.smart.tool.util;

import com.tce.smart.tool.enums.DeviceCapabilityEnum;

/**
 * 设备能力工具类
 * 提供设备能力判断的便捷方法
 * @author AI助手
 * @date 2025-06-24
 */
public class DeviceCapabilityUtil {

    /**
     * 判断设备是否支持人脸功能
     * @param deviceCapability 设备能力代码
     * @return true-支持人脸，false-不支持
     */
    public static boolean supportsFace(Integer deviceCapability) {
        return DeviceCapabilityEnum.supportsFace(deviceCapability);
    }

    /**
     * 判断设备是否支持刷卡功能
     * @param deviceCapability 设备能力代码
     * @return true-支持刷卡，false-不支持
     */
    public static boolean supportsCard(Integer deviceCapability) {
        return DeviceCapabilityEnum.supportsCard(deviceCapability);
    }

    /**
     * 判断设备是否同时支持人脸和刷卡功能
     * @param deviceCapability 设备能力代码
     * @return true-同时支持，false-不同时支持
     */
    public static boolean supportsBoth(Integer deviceCapability) {
        return DeviceCapabilityEnum.FACE_CARD.getCode().equals(deviceCapability);
    }

    /**
     * 判断设备是否仅支持人脸功能
     * @param deviceCapability 设备能力代码
     * @return true-仅支持人脸，false-不是仅支持人脸
     */
    public static boolean supportsFaceOnly(Integer deviceCapability) {
        return DeviceCapabilityEnum.FACE_ONLY.getCode().equals(deviceCapability);
    }

    /**
     * 判断设备是否仅支持刷卡功能
     * @param deviceCapability 设备能力代码
     * @return true-仅支持刷卡，false-不是仅支持刷卡
     */
    public static boolean supportsCardOnly(Integer deviceCapability) {
        return DeviceCapabilityEnum.CARD_ONLY.getCode().equals(deviceCapability);
    }

    /**
     * 获取设备能力描述
     * @param deviceCapability 设备能力代码
     * @return 设备能力描述
     */
    public static String getCapabilityDesc(Integer deviceCapability) {
        return DeviceCapabilityEnum.desc(deviceCapability);
    }

    /**
     * 验证设备能力代码是否有效
     * @param deviceCapability 设备能力代码
     * @return true-有效，false-无效
     */
    public static boolean isValidCapability(Integer deviceCapability) {
        return DeviceCapabilityEnum.existCapability(deviceCapability);
    }

    /**
     * 获取默认设备能力（人脸+刷卡）
     * @return 默认设备能力代码
     */
    public static Integer getDefaultCapability() {
        return DeviceCapabilityEnum.FACE_CARD.getCode();
    }

    /**
     * 根据设备能力判断是否需要下发人脸数据
     * @param deviceCapability 设备能力代码
     * @return true-需要下发人脸数据，false-不需要
     */
    public static boolean needsFaceData(Integer deviceCapability) {
        return supportsFace(deviceCapability);
    }

    /**
     * 根据设备能力判断是否需要下发卡片数据
     * @param deviceCapability 设备能力代码
     * @return true-需要下发卡片数据，false-不需要
     */
    public static boolean needsCardData(Integer deviceCapability) {
        return supportsCard(deviceCapability);
    }
}