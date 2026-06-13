package com.tce.smart.common.core.constant.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 警报类型
 * @author Lenovo
 *
 */
public enum AlarmType {

	ILLEGAL_INTRUDING(1, "非法闯入"),

	STRANGER(2,"陌生人警报");

    private Integer code;
    private String desc;

    AlarmType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AlarmType alarmType(Integer code){
        if(Objects.nonNull(code)){
            for(AlarmType alarmType : AlarmType.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	AlarmType alarmType = alarmType(code);
        return alarmType == null ? null : alarmType(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(AlarmType sex : AlarmType.values()){
                if(sex.desc.equals(desc)){
                    return sex.code;
                }
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
