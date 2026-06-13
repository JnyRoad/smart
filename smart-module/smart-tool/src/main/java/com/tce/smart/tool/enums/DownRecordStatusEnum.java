package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 下发数据的状态
 * @author ly
 *
 */
public enum DownRecordStatusEnum {


	STATUS_1(1,"成功"),
	STATUS_2(2,"失败");


    private final Integer code;

    private final String desc;

    DownRecordStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DownRecordStatusEnum downRecordStatusAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(DownRecordStatusEnum alarmType : DownRecordStatusEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	DownRecordStatusEnum alarmType = downRecordStatusAuthority(code);
        return alarmType == null ? null : downRecordStatusAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(DownRecordStatusEnum deviceAuthority : DownRecordStatusEnum.values()){
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
            for(DownRecordStatusEnum alarmType : DownRecordStatusEnum.values()){
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
