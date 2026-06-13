package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 设备
 * @author Lenovo
 *
 */
public enum DeviceTypeEnum {

	/**
	 * 设备类型
	 */
	DEVICE_TYPE_1(1, "闸机"),
	DEVICE_TYPE_2(2, "门禁"),
	DEVICE_TYPE_3(3, "道闸"),
	DEVICE_TYPE_4(4, "摄像头");


    private final Integer code;

    private final String desc;

    DeviceTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DeviceTypeEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(DeviceTypeEnum alarmType : DeviceTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	DeviceTypeEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : deviceAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(DeviceTypeEnum deviceAuthority : DeviceTypeEnum.values()){
                if(deviceAuthority.desc.equals(desc)){
                    return deviceAuthority.code;
                }
            }
        }
        return null;
    }

    public static boolean existAuthority(Integer code){
	boolean result = false;
        if(Objects.nonNull(code)){
            for(DeviceTypeEnum alarmType : DeviceTypeEnum.values()){
	result = alarmType.code.equals(code);
	if(result) {
		return result;
	}
            }
        }
        return result;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
