package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 离职类型
 * @author Lenovo
 *
 */
public enum LeaveApplicationEnum {

    NORMAL(0, "正常"),

    ABNORMAL(1, "异常");

    private final Integer code;

    private final String desc;

    LeaveApplicationEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LeaveApplicationEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(LeaveApplicationEnum tempEnum : LeaveApplicationEnum.values()){
                if(tempEnum.code.equals(code)){
                    return tempEnum;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	LeaveApplicationEnum tempEnum = deviceAuthority(code);
        return tempEnum == null ? null : deviceAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(LeaveApplicationEnum tempEnum : LeaveApplicationEnum.values()){
                if(tempEnum.desc.equals(desc)){
                    return tempEnum.code;
                }
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
