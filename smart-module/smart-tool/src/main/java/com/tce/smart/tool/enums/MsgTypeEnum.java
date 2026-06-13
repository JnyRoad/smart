package com.tce.smart.tool.enums;

import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

public enum MsgTypeEnum {
	MSG_1(1,"短信"),
	MSG_2(2,"邮箱"),
	MSG_3(3,"App消息"),
	MSG_4(4,"微信推送消息");

    private final Integer code;

    private final String desc;

    MsgTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MsgTypeEnum visitorAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(MsgTypeEnum alarmType : MsgTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	MsgTypeEnum alarmType = visitorAuthority(code);
        return alarmType == null ? null : visitorAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(MsgTypeEnum deviceAuthority : MsgTypeEnum.values()){
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
            for(MsgTypeEnum alarmType : MsgTypeEnum.values()){
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
