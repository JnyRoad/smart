package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 访客的状态
 * @author ly
 *
 */
public enum AttendanceEnum {


	Is_CHECK_STATE(0,"正常"),
	Not_CHECK_STATE(1,"异常");

    private final Integer code;

    private final String desc;

    AttendanceEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AttendanceEnum attendanceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(AttendanceEnum alarmType : AttendanceEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	AttendanceEnum alarmType = attendanceAuthority(code);
        return alarmType == null ? null : attendanceAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(AttendanceEnum deviceAuthority : AttendanceEnum.values()){
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
            for(AttendanceEnum alarmType : AttendanceEnum.values()){
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
