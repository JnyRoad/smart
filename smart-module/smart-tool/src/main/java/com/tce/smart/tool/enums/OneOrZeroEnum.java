package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 0 1 枚举
 * @author ly
 *
 */
public enum OneOrZeroEnum {


	ONE(1,"是"),
	ZERO(0,"否");

    private final Integer code;

    private final String desc;

    OneOrZeroEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OneOrZeroEnum overTimeAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(OneOrZeroEnum alarmType : OneOrZeroEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	OneOrZeroEnum alarmType = overTimeAuthority(code);
        return alarmType == null ? null : overTimeAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(OneOrZeroEnum deviceAuthority : OneOrZeroEnum.values()){
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
            for(OneOrZeroEnum alarmType : OneOrZeroEnum.values()){
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
