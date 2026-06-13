package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 出差类型：国内还是国外
 * @author liangyuan
 *
 */
public enum TripTypeEnum {


	TRIP_TYPE_0(0,"国内"),
	TRIP_TYPE_1(1,"国际");

    private final Integer code;

    private final String desc;

    TripTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TripTypeEnum tripTypeAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(TripTypeEnum alarmType : TripTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	TripTypeEnum tripType = tripTypeAuthority(code);
        return tripType == null ? null : tripTypeAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(TripTypeEnum tripTypeAuthority : TripTypeEnum.values()){
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
            for(TripTypeEnum alarmType : TripTypeEnum.values()){
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
