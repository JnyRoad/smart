package com.tce.smart.tool.enums;

import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

public enum DormitorySexEnum {



	MAN_ROOM(0,"男"),
	WOMAN_ROOM(1,"女"),
	MAN_WOMAN_ROOM(2,"夫妻"),
	OTHER_ROOM(3,"其他");

    private final Integer code;

    private final String desc;

    DormitorySexEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DormitorySexEnum eventTypeAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(DormitorySexEnum alarmType : DormitorySexEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	DormitorySexEnum alarmType = eventTypeAuthority(code);
        return alarmType == null ? null : eventTypeAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(DormitorySexEnum deviceAuthority : DormitorySexEnum.values()){
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
            for(DormitorySexEnum alarmType : DormitorySexEnum.values()){
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
