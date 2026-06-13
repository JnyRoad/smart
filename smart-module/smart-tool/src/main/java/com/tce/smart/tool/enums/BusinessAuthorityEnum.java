package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 设备权限
 * @author Lenovo
 *
 */
public enum BusinessAuthorityEnum {

	VISITOR_FACE(1, "访客人员通行权限"),

	VISITOR_VEHICLE(2, "访客车辆通行权限"),

	STAFF_FACE(4,"员工刷脸通行权限"),

	IN_OUT_STAFF_VEHICLE(5,"员工车辆通行权限"),

	PARK_VEHICLE(6,"园区车辆权限"),

	LOGISTICS_APPOINTMENT(7,"物流车通行权限"),

	NOT_STAFF_VEHICLE(8,"非员工车辆通行权限"),

	IN_STAFF_VEHICLE(9,"公司车辆通行权限"),

	SPECIAL_JCHE(10,"特殊职层车辆通行权限");



    private final Integer code;

    private final String desc;

    BusinessAuthorityEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BusinessAuthorityEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(BusinessAuthorityEnum alarmType : BusinessAuthorityEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	BusinessAuthorityEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : deviceAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(BusinessAuthorityEnum deviceAuthority : BusinessAuthorityEnum.values()){
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
            for(BusinessAuthorityEnum alarmType : BusinessAuthorityEnum.values()){
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
