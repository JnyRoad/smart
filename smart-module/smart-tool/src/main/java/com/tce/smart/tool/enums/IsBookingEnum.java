package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 出差是否需要订机票
 * @author liangyuan
 *
 */
public enum IsBookingEnum {


	IS_BOOKING_0(0,"是"),
	IS_BOOKING_1(1,"否");

    private final Integer code;

    private final String desc;

    IsBookingEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static IsBookingEnum isBookingAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(IsBookingEnum isBookingType : IsBookingEnum.values()){
                if(isBookingType.code.equals(code)){
                    return isBookingType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	IsBookingEnum isBookingType = isBookingAuthority(code);
        return isBookingType == null ? null : isBookingAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(IsBookingEnum tripTypeAuthority : IsBookingEnum.values()){
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
            for(IsBookingEnum alarmType : IsBookingEnum.values()){
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
