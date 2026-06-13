package com.tce.smart.tool.enums;


import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

/**
 * 设备权限
 * @author Lenovo
 *
 */
public enum DeviceAuthorityEnum {

	VISITOR(1, "访客权限"),

	VISITOR_VEHICLE(2, "访客车辆权限"),

	INTERVIEWER(3, "面试人员权限"),

	STAFF(4,"员工权限"),

	IN_OUT_STAFF_VEHICLE(5,"员工进出车辆权限"),

	PARK_VEHICLE(6,"园区车辆权限"),

	LOGISTICS_APPOINTMENT(7,"物流车权限"),

	NOT_STAFF_VEHICLE(8,"非员工车辆权限"),

	IN_STAFF_VEHICLE(9,"员工进车辆权限");

    private final Integer code;

    private final String desc;

    DeviceAuthorityEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DeviceAuthorityEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(DeviceAuthorityEnum alarmType : DeviceAuthorityEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	DeviceAuthorityEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : deviceAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(DeviceAuthorityEnum deviceAuthority : DeviceAuthorityEnum.values()){
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
            for(DeviceAuthorityEnum alarmType : DeviceAuthorityEnum.values()){
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
