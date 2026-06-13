package com.tce.smart.common.core.constant.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

public enum SexType {
    MAN(0, "男"),

    WOMAN(1, "女"),

    UNKNOWN(2,"未知");

    private Integer code;
    private String desc;

    SexType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SexType sex(Integer code){
        if(Objects.nonNull(code)){
            for(SexType sex : SexType.values()){
                if(sex.code.equals(code)){
                    return sex;
                }
            }
        }
        return SexType.UNKNOWN;
    }
    public static String desc(Integer code){
        return sex(code).desc;
    }
    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(SexType sex : SexType.values()){
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
