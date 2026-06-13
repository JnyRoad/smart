package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 抄表类型枚举
 * @author wuling
 *
 */
public enum MeterTypeEnum {

	ROOM_METER(1, "房间抄表"),
	COMMON_METER(2, "公摊抄表");


    private final Integer code;

    private final String desc;

    MeterTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MeterTypeEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(MeterTypeEnum alarmType : MeterTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	MeterTypeEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : alarmType.desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(MeterTypeEnum deviceAuthority : MeterTypeEnum.values()){
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
            for(MeterTypeEnum alarmType : MeterTypeEnum.values()){
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
