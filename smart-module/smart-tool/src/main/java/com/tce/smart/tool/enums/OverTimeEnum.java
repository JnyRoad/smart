package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 访客的状态
 * @author ly
 *
 */
public enum OverTimeEnum {


	Is_TRAVEL_WORK(1,"是"),
	Not_TRAVEL_WORK(0,"否");

    private final Integer code;

    private final String desc;

    OverTimeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OverTimeEnum overTimeAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(OverTimeEnum alarmType : OverTimeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	OverTimeEnum alarmType = overTimeAuthority(code);
        return alarmType == null ? null : overTimeAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(OverTimeEnum deviceAuthority : OverTimeEnum.values()){
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
            for(OverTimeEnum alarmType : OverTimeEnum.values()){
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
