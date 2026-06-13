package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 设备权限
 * @author Lenovo
 *
 */
public enum VehicleEventTypEnum {


	IN(1, "进"),

	OUT(2, "出");


    private final Integer code;

    private final String desc;

    VehicleEventTypEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static VehicleEventTypEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(VehicleEventTypEnum alarmType : VehicleEventTypEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	VehicleEventTypEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : deviceAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(VehicleEventTypEnum deviceAuthority : VehicleEventTypEnum.values()){
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
            for(VehicleEventTypEnum alarmType : VehicleEventTypEnum.values()){
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
