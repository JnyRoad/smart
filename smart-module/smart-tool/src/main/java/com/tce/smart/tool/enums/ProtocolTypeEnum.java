package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 协议类型枚举
 * @author wuling
 *
 */
public enum ProtocolTypeEnum {

	IMG(1, "图片"),
	PDF(2, "PDF");


    private final Integer code;

    private final String desc;

    ProtocolTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProtocolTypeEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(ProtocolTypeEnum alarmType : ProtocolTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	ProtocolTypeEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : alarmType.desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(ProtocolTypeEnum deviceAuthority : ProtocolTypeEnum.values()){
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
            for(ProtocolTypeEnum alarmType : ProtocolTypeEnum.values()){
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
