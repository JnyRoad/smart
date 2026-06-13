package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 证件类型枚举
 * @author wuling
 *
 */
public enum CertTypeEnum {

	TYPE_1(1, "二代身份证"),
	TYPE_2(2, "港澳通行证"),
	TYPE_3(3, "护照"),
	OTHER(4,"其他");


    private final Integer code;

    private final String desc;

    CertTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CertTypeEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(CertTypeEnum alarmType : CertTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	CertTypeEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : alarmType.desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(CertTypeEnum deviceAuthority : CertTypeEnum.values()){
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
            for(CertTypeEnum alarmType : CertTypeEnum.values()){
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
