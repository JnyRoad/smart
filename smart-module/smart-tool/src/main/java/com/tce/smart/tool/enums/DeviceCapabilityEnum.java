package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 设备能力枚举
 * 标识设备支持的功能类型
 * @author AI助手
 * @date 2025-06-24
 */
public enum DeviceCapabilityEnum {

    /**
     * 设备能力类型
     */
    FACE_ONLY(1, "仅人脸识别"),
    CARD_ONLY(2, "仅刷卡"),
    FACE_CARD(3, "人脸+刷卡");

    private final Integer code;

    private final String desc;

    DeviceCapabilityEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DeviceCapabilityEnum getByCode(Integer code){
        if(Objects.nonNull(code)){
            for(DeviceCapabilityEnum capability : DeviceCapabilityEnum.values()){
                if(capability.code.equals(code)){
                    return capability;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
        DeviceCapabilityEnum capability = getByCode(code);
        return capability == null ? null : capability.desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(DeviceCapabilityEnum capability : DeviceCapabilityEnum.values()){
                if(capability.desc.equals(desc)){
                    return capability.code;
                }
            }
        }
        return null;
    }

    public static boolean existCapability(Integer code){
        boolean result = false;
        if(Objects.nonNull(code)){
            for(DeviceCapabilityEnum capability : DeviceCapabilityEnum.values()){
                result = capability.code.equals(code);
                if(result) {
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * 判断设备是否支持人脸功能
     * @param code 设备能力代码
     * @return true-支持人脸，false-不支持
     */
    public static boolean supportsFace(Integer code) {
        return FACE_ONLY.code.equals(code) || FACE_CARD.code.equals(code);
    }

    /**
     * 判断设备是否支持刷卡功能
     * @param code 设备能力代码
     * @return true-支持刷卡，false-不支持
     */
    public static boolean supportsCard(Integer code) {
        return CARD_ONLY.code.equals(code) || FACE_CARD.code.equals(code);
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}