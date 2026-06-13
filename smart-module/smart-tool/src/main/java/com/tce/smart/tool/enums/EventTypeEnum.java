package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 访客的状态
 * @author ly
 *
 */
public enum EventTypeEnum {


	EVENT_TYPE_1(1,"进门"),
	EVENT_TYPE_2(2,"出门");

    private final Integer code;

    private final String desc;

    EventTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static EventTypeEnum eventTypeAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(EventTypeEnum alarmType : EventTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	EventTypeEnum alarmType = eventTypeAuthority(code);
        return alarmType == null ? null : eventTypeAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(EventTypeEnum deviceAuthority : EventTypeEnum.values()){
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
            for(EventTypeEnum alarmType : EventTypeEnum.values()){
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
