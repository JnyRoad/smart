package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

public enum PushVistorEnum {


	PSHT_DAY(0,"每天"),
	PSHT_WEEK(1,"每一周"),
	PSHT_MONTH(2,"每一月");

	PushVistorEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	private final Integer code;
	private final String desc;

	public Integer getCode() {
		return this.code;
	}
	public String getDesc() {
		return this.desc;
	}

    public static PushVistorEnum staffAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(PushVistorEnum alarmType : PushVistorEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	PushVistorEnum alarmType = staffAuthority(code);
        return alarmType == null ? null : staffAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(PushVistorEnum deviceAuthority : PushVistorEnum.values()){
                if(deviceAuthority.desc.equals(desc)){
                    return deviceAuthority.code;
                }
            }
        }
        return null;
    }


}
