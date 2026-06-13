package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 出差是否需要订机票
 * @author liangyuan
 *
 */
public enum IsAttendanceEnum {


	IS_ATTEND_0(0,"true"),
	IS_ATTEND_1(1,"false");

    private final Integer code;

    private final String desc;

    IsAttendanceEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static IsAttendanceEnum isAttendanceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(IsAttendanceEnum isBookingType : IsAttendanceEnum.values()){
                if(isBookingType.code.equals(code)){
                    return isBookingType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	IsAttendanceEnum isBookingType = isAttendanceAuthority(code);
        return isBookingType == null ? null : isAttendanceAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(IsAttendanceEnum tripTypeAuthority : IsAttendanceEnum.values()){
                if(tripTypeAuthority.desc.equals(desc)){
                    return tripTypeAuthority.code;
                }
            }
        }
        return null;
    }

    public static boolean existAuthority(Integer code){
	boolean result = false;
        if(Objects.nonNull(code)){
            for(IsAttendanceEnum alarmType : IsAttendanceEnum.values()){
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
