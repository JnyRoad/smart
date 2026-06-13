package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 床位类型枚举
 *
 * @author wuling
 *
 */
public enum BedTypeEnum {

	UPPER(1, "上铺"),
	LOWER(2, "下铺");


    private final Integer code;

    private final String desc;

    BedTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BedTypeEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(BedTypeEnum alarmType : BedTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	BedTypeEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : alarmType.desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(BedTypeEnum deviceAuthority : BedTypeEnum.values()){
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
            for(BedTypeEnum alarmType : BedTypeEnum.values()){
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
