package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 是否放行
 * @author ly
 *
 */
public enum LetPassEnum {


	LET_PASS_0(0,"未放行"),
	LET_PASS_1(1,"放行"),
	LET_PASS_2(2,"未知");

    private final Integer code;

    private final String desc;

    LetPassEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LetPassEnum letPassAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(LetPassEnum alarmType : LetPassEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	LetPassEnum alarmType = letPassAuthority(code);
        return alarmType == null ? null : letPassAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(LetPassEnum deviceAuthority : LetPassEnum.values()){
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
            for(LetPassEnum alarmType : LetPassEnum.values()){
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
